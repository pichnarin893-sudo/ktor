#!/bin/bash

# Workflow Validation Script
# This script validates GitHub Actions workflow files

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

WORKFLOWS_DIR=".github/workflows"
ERRORS=0

echo -e "${YELLOW}Validating GitHub Actions workflows...${NC}\n"

# Check if workflows directory exists
if [ ! -d "$WORKFLOWS_DIR" ]; then
    echo -e "${RED}Error: Workflows directory not found at $WORKFLOWS_DIR${NC}"
    exit 1
fi

# Function to validate YAML syntax
validate_yaml() {
    local file=$1
    echo -n "Validating $file... "

    # Check if file exists
    if [ ! -f "$file" ]; then
        echo -e "${RED}✗ File not found${NC}"
        return 1
    fi

    # Basic YAML syntax check (requires python or yq)
    if command -v python3 &> /dev/null; then
        if python3 -c "import yaml; yaml.safe_load(open('$file'))" 2>/dev/null; then
            echo -e "${GREEN}✓ Valid YAML${NC}"
            return 0
        else
            echo -e "${RED}✗ Invalid YAML syntax${NC}"
            return 1
        fi
    elif command -v yq &> /dev/null; then
        if yq eval '.' "$file" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ Valid YAML${NC}"
            return 0
        else
            echo -e "${RED}✗ Invalid YAML syntax${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠ Skipped (install python3 or yq for validation)${NC}"
        return 0
    fi
}

# Function to check required fields in workflow
check_workflow_structure() {
    local file=$1
    local filename=$(basename "$file")

    echo -n "Checking structure of $filename... "

    # Check for required fields
    if ! grep -q "^name:" "$file"; then
        echo -e "${RED}✗ Missing 'name' field${NC}"
        return 1
    fi

    if ! grep -q "^on:" "$file"; then
        echo -e "${RED}✗ Missing 'on' trigger${NC}"
        return 1
    fi

    if ! grep -q "^jobs:" "$file"; then
        echo -e "${RED}✗ Missing 'jobs' section${NC}"
        return 1
    fi

    echo -e "${GREEN}✓ Structure looks good${NC}"
    return 0
}

# Validate all workflow files
for workflow in "$WORKFLOWS_DIR"/*.yml "$WORKFLOWS_DIR"/*.yaml; do
    if [ -f "$workflow" ]; then
        validate_yaml "$workflow" || ERRORS=$((ERRORS + 1))
        check_workflow_structure "$workflow" || ERRORS=$((ERRORS + 1))
        echo ""
    fi
done

# Check for common issues
echo "--- Common Issues Check ---"

# Check for hardcoded secrets
echo -n "Checking for hardcoded secrets... "
if grep -r -i "password\|secret\|token\|key" "$WORKFLOWS_DIR"/*.yml | grep -v "secrets\." | grep -v "github.token" | grep -q ":\s*['\"]"; then
    echo -e "${YELLOW}⚠ Warning: Possible hardcoded secrets found${NC}"
    echo "  Please review and use GitHub Secrets instead"
    ERRORS=$((ERRORS + 1))
else
    echo -e "${GREEN}✓ No hardcoded secrets detected${NC}"
fi

# Check for deprecated actions
echo -n "Checking for deprecated actions... "
if grep -r "actions/checkout@v[12]" "$WORKFLOWS_DIR" > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠ Warning: Deprecated checkout action version${NC}"
    echo "  Consider upgrading to actions/checkout@v4"
else
    echo -e "${GREEN}✓ No deprecated actions found${NC}"
fi

# Check for proper caching
echo -n "Checking for Gradle caching... "
if grep -r "setup-java" "$WORKFLOWS_DIR" | grep -q "cache: 'gradle'"; then
    echo -e "${GREEN}✓ Gradle caching is configured${NC}"
else
    echo -e "${YELLOW}⚠ Warning: Gradle caching not found${NC}"
    echo "  Consider adding cache: 'gradle' to setup-java steps"
fi

echo ""
echo "==================================="

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}All validations passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Found $ERRORS issue(s) ✗${NC}"
    echo "Please review and fix the issues above"
    exit 1
fi
