## Alternative Solution for Bottom Navigation Overlap

If you want a more robust solution that adapts to different screen configurations, you can add this to your Fragment:

### Option 1: WindowInsets Approach (Recommended)

```java
// Add this to your PalletManagementFragment.java setupViews() method:

private void setupWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
        
        // Apply bottom margin to pagination layout
        ViewGroup.MarginLayoutParams params = 
            (ViewGroup.MarginLayoutParams) binding.paginationLayout.getLayoutParams();
        params.bottomMargin = Math.max(systemBars.bottom, navigationBars.bottom) + 
                             getResources().getDimensionPixelSize(R.dimen.bottom_navigation_height);
        binding.paginationLayout.setLayoutParams(params);
        
        return insets;
    });
}
```

### Option 2: Add dimension resource (Current approach - simpler)

Add to `res/values/dimens.xml`:
```xml
<dimen name="bottom_navigation_height">56dp</dimen>
```

Then use in layout:
```xml
android:layout_marginBottom="@dimen/bottom_navigation_height"
```

### Option 3: Programmatic margin adjustment

```java
// Add this method to your Fragment:
private void adjustForBottomNavigation() {
    // Get the bottom app bar height
    TypedValue tv = new TypedValue();
    getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
    int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
    
    // Apply margin to pagination layout
    ViewGroup.MarginLayoutParams params = 
        (ViewGroup.MarginLayoutParams) binding.paginationLayout.getLayoutParams();
    params.bottomMargin = actionBarHeight;
    binding.paginationLayout.setLayoutParams(params);
}
```

The current solution using `?attr/actionBarSize` should work well for most cases!