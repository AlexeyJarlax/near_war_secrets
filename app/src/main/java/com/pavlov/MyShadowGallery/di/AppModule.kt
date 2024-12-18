package com.pavlov.MyShadowGallery.di

import android.content.Context
import com.pavlov.MyShadowGallery.data.repository.ImageRepository
import com.pavlov.MyShadowGallery.data.utils.ImageUriHelper
import com.pavlov.MyShadowGallery.data.repository.SteganographyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideImageUriHelper(
        @ApplicationContext context: Context
    ): ImageUriHelper {
        return ImageUriHelper(context)
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        @ApplicationContext context: Context,
        imageUriHelper: ImageUriHelper
    ): ImageRepository {
        return ImageRepository(context, imageUriHelper)
    }

    @Provides
    @Singleton
    fun provideSteganographyRepository(
        @ApplicationContext context: Context
    ): SteganographyRepository {
        return SteganographyRepository(context)
    }
}