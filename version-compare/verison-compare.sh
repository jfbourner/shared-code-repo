#!/bin/bash

set -e

# ===== CONFIGURE THESE =====
GITHUB_HOST="github.com"           # Your GHE hostname
GITHUB_ORG="jack"                  # Your GitHub org/user
OPENSHIFT_URL=""                   # Your OpenShift cluster URL (optional, will prompt if empty)
USERNAME_SUFFIX="@test.account"    # Suffix appended to username (e.g. @test.account)
# ===========================

usage() {
    echo "Usage: $0 -n <namespace> -f <csv-file> [-g <github-host>] [-o <github-org>] [-c <openshift-url>]"
    echo "  -n  OpenShift namespace"
    echo "  -f  CSV file (format: deployment-name,repo-name)"
    echo "  -g  GitHub host (default: $GITHUB_HOST)"
    echo "  -o  GitHub org/user (default: $GITHUB_ORG)"
    echo "  -c  OpenShift cluster URL"
    exit 1
}

while getopts "n:f:g:o:c:" opt; do
    case $opt in
        n) NAMESPACE="$OPTARG" ;;
        f) CSV_FILE="$OPTARG" ;;
        g) GITHUB_HOST="$OPTARG" ;;
        o) GITHUB_ORG="$OPTARG" ;;
        c) OPENSHIFT_URL="$OPTARG" ;;
        *) usage ;;
    esac
done

[[ -z "$NAMESPACE" || -z "$CSV_FILE" ]] && usage
[[ ! -f "$CSV_FILE" ]] && echo "Error: CSV file not found: $CSV_FILE" && exit 1

# Check OpenShift login, prompt if needed
if ! oc whoami &>/dev/null; then
    echo "Not logged into OpenShift. Please login:"
    if [[ -z "$OPENSHIFT_URL" ]]; then
        read -p "OpenShift URL: " OPENSHIFT_URL
    fi
    read -p "Username: " OC_USER
    read -sp "Password: " OC_PASS
    echo
    oc login "$OPENSHIFT_URL" -u "${OC_USER}${USERNAME_SUFFIX}" -p "$OC_PASS"
fi

# Check gh CLI auth
if ! gh auth status --hostname "$GITHUB_HOST" &>/dev/null; then
    echo "Not logged into GitHub ($GITHUB_HOST). Initiating login..."
    gh auth login --hostname "$GITHUB_HOST" --web
fi

# Print header
printf "%-30s %-20s %-20s %-10s\n" "DEPLOYMENT" "K8S VERSION" "LATEST TAG" "STATUS"
printf "%-30s %-20s %-20s %-10s\n" "----------" "-----------" "----------" "------"

# Process each line in CSV
while IFS=',' read -r deployment repo || [[ -n "$deployment" ]]; do
    # Trim whitespace
    deployment=$(echo "$deployment" | xargs)
    repo=$(echo "$repo" | xargs)

    # Skip empty lines or comments
    [[ -z "$deployment" || "$deployment" == \#* ]] && continue

    # Get version label from OpenShift deployment
    k8s_version=$(oc get deployment "$deployment" -n "$NAMESPACE" \
        -o jsonpath='{.metadata.labels.version}' 2>/dev/null || echo "NOT FOUND")

    [[ -z "$k8s_version" ]] && k8s_version="NO LABEL"

    # Get latest tag from GitHub (filtered by semantic version pattern)
    # Matches: 1.0, 1.0-RC, 1.0.0, 1.0.0-RC, etc.
    latest_tag=$(gh api "repos/${GITHUB_ORG}/${repo}/tags" --hostname "$GITHUB_HOST" \
        --jq '.[].name' 2>/dev/null \
        | grep -E '^[0-9]+\.[0-9]+(\.[0-9]+)?(-RC)?$' \
        | sort -V \
        | tail -1)

    [[ -z "$latest_tag" ]] && latest_tag="NO TAGS"

    # Compare
    if [[ "$k8s_version" == "$latest_tag" ]]; then
        status="✓ MATCH"
    elif [[ "$k8s_version" == "NOT FOUND" || "$k8s_version" == "NO LABEL" ]]; then
        status="? K8S"
    elif [[ "$latest_tag" == "NO TAGS" ]]; then
        status="? GH"
    else
        status="✗ DIFFER"
    fi

    printf "%-30s %-20s %-20s %-10s\n" "$deployment" "$k8s_version" "$latest_tag" "$status"

done < "$CSV_FILE"