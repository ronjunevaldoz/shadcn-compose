# Step 9: Source File Stubs

Part of `kmp-feature-scaffold`. Load this file when working on: step 9: source file stubs.

---

After creating build files, generate stub source files so each module compiles:

### `:feature:FEATURE_NAME:model`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/model/
    FEATURE_NAMEModel.kt             ← data class(es), sealed types, enums
```

### `:feature:FEATURE_NAME:api`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/api/
    FEATURE_NAMERepository.kt        ← interface (uses types from :model)
    FEATURE_NAMENavigation.kt        ← nav route objects/sealed class
```

### `:feature:FEATURE_NAME:domain`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/domain/
    Get<FEATURE_NAME>UseCase.kt
    di/FEATURE_NAME_DomainModule.kt  ← only in manual mode
```

### `:feature:FEATURE_NAME:data`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/data/
    FEATURE_NAMERepositoryImpl.kt
    remote/FEATURE_NAMERemoteDataSource.kt
    local/FEATURE_NAMELocalDataSource.kt
    di/FEATURE_NAME_DataModule.kt    ← only in manual mode
```

### `:feature:FEATURE_NAME:presenter`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/presenter/
    FEATURE_NAMEViewModel.kt         ← ViewModel, no Compose import
    FEATURE_NAMEUiState.kt           ← MVI state sealed class
    FEATURE_NAMEUiIntent.kt          ← MVI intent sealed class
    di/FEATURE_NAME_PresenterModule.kt  ← only in manual mode
```

### `:feature:FEATURE_NAME:ui`
```
src/commonMain/kotlin/GROUP_ID/feature/FEATURE_NAME/ui/
    FEATURE_NAMEScreen.kt            ← wires ViewModel from :presenter via koinViewModel()
    FEATURE_NAMEContent.kt           ← stateless @Composable, accepts state parameter
    previews/
        FEATURE_NAMEContentPreview.kt ← required preview stub for the Content composable
```

---

