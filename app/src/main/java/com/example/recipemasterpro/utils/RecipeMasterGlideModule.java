package com.example.recipemasterpro.utils;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Global Glide configuration module.
 * This class triggers the generation of the GlideApp API.
 */
@GlideModule
public final class RecipeMasterGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        // Disable manifest parsing to avoid looking for legacy GlideModules
        return false;
    }
}
