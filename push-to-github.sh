#!/bin/bash

# Script to push to GitHub repository
# Run this script after creating a repository on GitHub

echo "🚀 GitHub Push Script"
echo "===================="
echo ""

# Check if repository name is provided
if [ -z "$1" ]; then
    echo "❌ Error: Please provide GitHub username"
    echo "Usage: ./push-to-github.sh <github-username>"
    echo ""
    echo "Example: ./push-to-github.sh yourusername"
    exit 1
fi

GITHUB_USERNAME=$1
REPO_NAME="mainframes-java-secure"

echo "📝 Instructions:"
echo ""
echo "1. Go to https://github.com/new"
echo "2. Repository name: $REPO_NAME"
echo "3. Description: Production-ready Java Spring Boot application for s390x OpenShift with comprehensive security hardening"
echo "4. Choose: Public"
echo "5. DO NOT initialize with README, .gitignore, or license"
echo "6. Click 'Create repository'"
echo ""
read -p "Press Enter after creating the repository on GitHub..."

echo ""
echo "🔗 Adding remote and pushing..."
git remote add origin "https://github.com/${GITHUB_USERNAME}/${REPO_NAME}.git"
git branch -M main
git push -u origin main

echo ""
echo "✅ Done! Your repository is now at:"
echo "   https://github.com/${GITHUB_USERNAME}/${REPO_NAME}"
echo ""
echo "🔐 Next steps for security:"
echo "1. Go to: https://github.com/${GITHUB_USERNAME}/${REPO_NAME}/settings/secrets/actions"
echo "2. Add these secrets (if deploying to OpenShift):"
echo "   - OPENSHIFT_SERVER"
echo "   - OPENSHIFT_TOKEN"
echo "   - OPENSHIFT_NAMESPACE"
echo "   - REGISTRY_USERNAME (Quay.io username)"
echo "   - REGISTRY_PASSWORD (Quay.io password)"
echo "   - REGISTRY_NAMESPACE (Quay.io organization)"
echo ""
echo "3. Enable Security Features:"
echo "   - Go to Settings → Security → Code security and analysis"
echo "   - Enable: Dependency graph, Dependabot alerts, Dependabot security updates"
echo "   - Enable: Code scanning"
echo ""
echo "📊 View Security Dashboard:"
echo "   https://github.com/${GITHUB_USERNAME}/${REPO_NAME}/security"
