#!/bin/bash

set -e

GITHUB_HOST="github.com"  # Override this for your GHE instance

usage() {
    echo "Usage: $0 -n <namespace> -f <csv-file> [-g <github-host>]"
    echo "  -n  OpenShift namespace"
    echo "  -f  CSV file (format: deployment-name,repo-name)"
    echo "  -g  GitHub host (default: github.com)"
    exit 1
}

while getopts "n:f:g:" opt; do
    case $opt in
        n) NAMESPACE="$OPTARG" ;;
        f) CSV_FILE="$OPTARG" ;;
        g) GITHUB_HOST="$OPTARG" ;;
        *) usage ;;
    esac
done

[[ -z "$NAMESPACE" || -z "$CSV_FILE" ]] && usage
[[ ! -f "$CSV_FILE" ]] && echo "Error: CSV file not found: $CSV_FILE" && exit 1

# Check OpenShift login, prompt if needed
if ! oc whoami &>/dev/null; then
    echo "Not logged into OpenShift. Please login:"
    read -p "OpenShift URL: " OC_URL
    read -p "Username: " OC_USER
    read -sp "Password: " OC_PASS
    echo
    oc login "$OC_URL" -u "$OC_USER" -p "$OC_PASS"
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

    # Get latest tag from GitHub
    latest_tag=$(gh api "repos/jack/${repo}/tags" --hostname "$GITHUB_HOST" \
        --jq '.[0].name' 2>/dev/null || echo "NO TAGS")

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