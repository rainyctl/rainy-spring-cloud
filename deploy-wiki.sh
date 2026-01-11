#!/bin/bash

# Configuration
WIKI_REMOTE="git@github.com:rainyctl/rainy-spring-cloud.wiki.git"
WIKI_DIR="wiki"

# Ensure we are in the project root
if [ ! -d "$WIKI_DIR" ]; then
    echo "❌ Error: '$WIKI_DIR' directory not found. Please run this script from the project root."
    exit 1
fi

echo "🚀 Deploying Wiki to $WIKI_REMOTE..."

cd "$WIKI_DIR"

# Initialize Git if needed
if [ ! -d ".git" ]; then
    echo "Initializing Git repository..."
    git init
    git branch -m master
fi

# Set Remote
git remote remove origin 2>/dev/null
git remote add origin "$WIKI_REMOTE"

# Commit
git add .
git commit -m "Update Wiki content from repository" --allow-empty

# Push
echo "Pushing to GitHub..."
if git push -f origin master; then
    echo "✅ Wiki deployed successfully!"
    echo "👉 Check it here: https://github.com/rainyctl/rainy-spring-cloud/wiki"
else
    echo "❌ Push failed."
    echo "⚠️  IMPORTANT: Ensure you have clicked 'Create the first page' in the GitHub Wiki tab to initialize the repository first."
fi
