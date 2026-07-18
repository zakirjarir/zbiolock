#!/bin/bash

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

DATE=$(date +"%Y-%m-%d_%H-%M-%S")

clear

echo -e "${CYAN}"
echo "=================================================="
echo "               🚀 Git Helper Tool"
echo "=================================================="
echo -e "${NC}"

echo -e "${YELLOW}Select an option:${NC}"
echo
echo -e "  ${GREEN}[1]${NC} 📥 Pull latest changes from main"
echo -e "  ${GREEN}[2]${NC} 📤 Push changes to GitHub"
echo -e "  ${GREEN}[3]${NC} 🔀 Merge branch into main"
echo

read -r -p "👉 Enter option (1/2/3): " OPTION
echo

if [[ "$OPTION" == "1" ]]; then

    echo -e "${BLUE}📥 Pulling from origin/main...${NC}"
    echo

    git pull origin main

    echo
    echo -e "${GREEN}✅ Pull completed successfully.${NC}"

elif [[ "$OPTION" == "2" ]]; then

    echo -e "${BLUE}📤 Preparing GitHub backup...${NC}"
    echo

    git add .

    read -r -p "📝 Commit message (leave empty for auto message): " COMMIT_MSG

    if [[ -z "$COMMIT_MSG" ]]; then
        COMMIT_MSG="Backup on $DATE By Zakir"
    fi

    echo
    echo -e "${YELLOW}📦 Commit Message:${NC} $COMMIT_MSG"
    echo

    git commit -m "$COMMIT_MSG"
    git push origin main

    echo
    echo -e "${GREEN}✅ Successfully pushed to GitHub.${NC}"
    echo -e "${GREEN}📝 Commit:${NC} $COMMIT_MSG"

elif [[ "$OPTION" == "3" ]]; then

    read -r -p "🌿 Enter branch name to merge into main: " BRANCH
    echo

    echo -e "${BLUE}🔄 Switching to main branch...${NC}"
    git checkout main || exit 1

    echo
    echo -e "${BLUE}🔀 Merging '${BRANCH}' into main...${NC}"

    git merge "$BRANCH"

    echo
    echo -e "${GREEN}✅ Merge completed successfully.${NC}"

else

    echo -e "${RED}❌ Invalid option! Please choose 1, 2, or 3.${NC}"

fi

echo
echo -e "${CYAN}==================================================${NC}"
