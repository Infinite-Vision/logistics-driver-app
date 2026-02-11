# Module Structure Template

All feature modules in this application should follow this standardized structure:

## Directory Structure

```
modules/
  └── [moduleName]Module/
      ├── base/                  # Base classes for this module
      │   ├── BaseActivity.kt
      │   ├── BaseFragment.kt
      │   └── BaseViewModel.kt
      │
      ├── entity/                # Data entities/models for this module
      │   └── [Entity]Entity.kt
      │
      ├── view/                  # All UI-related code
      │   ├── activity/         # Activities
      │   ├── fragment/         # Fragments
      │   ├── adapter/          # RecyclerView adapters
      │   ├── custom/           # Custom views
      │   ├── interface/        # View interfaces
      │   ├── listener/         # Click listeners, callbacks
      │   └── enum/             # Enums used in views
      │
      └── viewModel/            # ViewModels for business logic
          └── [Feature]ViewModel.kt
```

## Module Conventions

1. **Package Naming**: Use `com.example.logistics_driver_app.modules.[moduleName]Module`
2. **Module Name**: Should be descriptive of the feature (e.g., `loginModule`, `homeModule`, `profileModule`)
3. **Base Classes**: Can be shared with other modules if needed
4. **Entities**: Should be specific to the module's domain
5. **View Components**: Organized by type (activity, fragment, adapter, etc.)
6. **ViewModels**: Follow the naming pattern `[Feature]ViewModel`

## Existing Modules

### loginModule ✅
Complete authentication and onboarding flow:
- Language selection
- Phone verification (OTP)
- Owner details
- Vehicle details  
- Driver details
- Verification progress

## Future Modules (Template)

When creating new modules, follow this checklist:

- [ ] Create module directory under `modules/`
- [ ] Add `base/` folder with Base classes if needed
- [ ] Add `entity/` folder for data models
- [ ] Add `view/` folder with appropriate subfolders
- [ ] Add `viewModel/` folder for business logic
- [ ] Update navigation graph if applicable
- [ ] Add module to this documentation

## Example: Creating a new module

```kotlin
// For a hypothetical "homeModule"
modules/
  └── homeModule/
      ├── base/
      │   └── HomeBaseFragment.kt
      ├── entity/
      │   ├── TripEntity.kt
      │   └── EarningsEntity.kt
      ├── view/
      │   ├── fragment/
      │   │   ├── HomeFragment.kt
      │   │   ├── TripListFragment.kt
      │   │   └── EarningsFragment.kt
      │   └── adapter/
      │       └── TripAdapter.kt
      └── viewModel/
          ├── HomeViewModel.kt
          └── TripViewModel.kt
```

## Notes

- Keep modules loosely coupled
- Use shared utilities from `Common/` package
- Database access should go through repositories in `data/` or module-specific data layer
- Navigation between modules should use Navigation Component with SafeArgs
