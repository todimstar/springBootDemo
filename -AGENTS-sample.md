# AGENTS.md

> **AI Coding Assistant Instructions** - This document guides AI tools (GitHub Copilot, Cursor, Claude, etc.) on how to work with this codebase effectively.

---

## Project Overview

**Description**: Add project description here

**Tech Stack**:
- **Framework**: React
- **Language**: TypeScript
- **Build Tool**: Not detected
- **Styling**: CSS Modules
- **State Management**: React Context API
- **Routing**: Not configured
- **Data Fetching**: fetch API
- **Forms**: Native forms
- **Validation**: Manual validation
- **Testing**: Not configured
- **Package Manager**: npm

---

## Quick Start

```bash
# Setup
npm install

# Development
npm run dev

# Build
npm run build

# Testing
npm run test

# Linting
npm run lint
```

---

## Project Structure

```
src/
├── components/
├── pages/
└── [Add your structure]
```



---

## Code Conventions

### General Guidelines

- **Language**: Use TypeScript for all files
- **Components**: Use functional components with hooks
- **File Naming**: PascalCase for components, camelCase for utilities

### Component Structure

```tsx
import { useState } from 'react';
import type { User } from '@/types';

interface UserCardProps {
  user: User;
  onEdit?: (id: string) => void;
}

export function UserCard({ user, onEdit }: UserCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  
  return (
    <div>
      {/* Component content */}
    </div>
  );
}
```

### Import Organization

```tsx
// 1. External dependencies
import { useState } from 'react';

// 2. Internal modules (use path aliases)
import { Button } from '@/components/ul/Button';
import { useAuth } from '@/hooks/useAuth';

// 3. Types
import type { User } from '@/types';

// 4. Styles (if applicable)
import styles from './Component.module.css';
```

---

## Styling Approach

**Primary Method**: CSS Modules

- One CSS module per component
- Use camelCase for class names
- Leverage composition with `composes`

---

## State Management

**Approach**: React Context API

- Create context providers in `src/context/`
- Separate context by domain
- Use custom hooks to access context

---

## Data Fetching

**Method**: fetch API

- All API calls should be organized in the services layer
- Use proper error handling and loading states
- Leverage fetch API features for caching and optimistic updates

---

## Routing

**Router**: Not configured



---

## Forms & Validation

**Forms**: Native forms
**Validation**: Manual validation



---

## Testing

**Framework**: Not configured

### Conventions

- Test file location: Co-located with components
- Naming: `ComponentName.test.tsx`
- Focus on user behavior and integration tests

---

## Environment Variables

**Location**: `.env.local`

See `.env.example` for required variables.

---

## Available Scripts

- `npm run dbhub` - dbhub
- `npm run install-tools` - npm install

---

## Path Aliases

Configured in `tsconfig.json`/`jsconfig.json`:

- `@/*` → `community-frontend/src/*`

---

## AI Assistant Guidelines

### When Generating Code

1. **Follow existing patterns**: Match the style and structure in the codebase
2. **Use type safety**: Always use TypeScript types
3. **Use path aliases**: Import using configured aliases
4. **Match styling approach**: Use CSS Modules conventions
5. **Follow state management**: Use React Context API patterns

### When Refactoring

1. Preserve functionality
2. Maintain type safety
3. Update related tests
4. Follow established conventions

---

**Last Generated**: 2026-02-08  
**Auto-generated from**: package.json, tsconfig.json, and project structure

> 💡 **Tip**: Use the Agent Automation dashboard to regenerate this file after major changes.
