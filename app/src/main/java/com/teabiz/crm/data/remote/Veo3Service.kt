package com.teabiz.crm.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Veo3Service @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class Veo3Prompt(
        val title: String,
        val prompt: String,
        val style: String,
        val duration: String,
        val aspectRatio: String,
        val cameraMovement: String,
        val lighting: String,
        val musicMood: String,
        val textOverlays: List<String>
    )

    data class VideoPlatform(
        val name: String,
        val url: String,
        val description: String,
        val free: Boolean
    )

    private val freePlatforms = listOf(
        VideoPlatform(
            name = "Google Veo 3",
            url = "https://deepmind.google/technologies/veo/",
            description = "Google's latest AI video generation model",
            free = true
        ),
        VideoPlatform(
            name = "Runway ML",
            url = "https://runwayml.com",
            description = "AI video generation and editing",
            free = true
        ),
        VideoPlatform(
            name = "Pika",
            url = "https://pika.art",
            description = "AI-powered video creation",
            free = true
        ),
        VideoPlatform(
            name = "CapCut",
            url = "https://www.capcut.com",
            description = "Free video editor with AI features",
            free = true
        ),
        VideoPlatform(
            name = "Canva",
            url = "https://www.canva.com",
            description = "Design and video creation",
            free = true
        ),
        VideoPlatform(
            name = "Synthesia",
            url = "https://synthesia.io",
            description = "AI avatar video generation",
            free = false
        ),
        VideoPlatform(
            name = "HeyGen",
            url = "https://heygen.com",
            description = "AI video with avatars",
            free = false
        )
    )

    fun getFreePlatforms(): List<VideoPlatform> = freePlatforms.filter { it.free }

    fun getAllPlatforms(): List<VideoPlatform> = freePlatforms

    fun openPlatform(platform: VideoPlatform) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(platform.url))
        context.startActivity(intent)
    }

    fun generateVeo3Prompt(
        product: String,
        style: String = "Cinematic",
        duration: String = "30 seconds",
        platform: String = "Instagram Reels"
    ): Veo3Prompt {
        val aspectRatio = when {
            platform.contains("Reels") || platform.contains("TikTok") || platform.contains("Shorts") -> "9:16"
            platform.contains("YouTube") && !platform.contains("Short") -> "16:9"
            platform.contains("LinkedIn") -> "16:9"
            else -> "1:1"
        }

        val cameraMovements = listOf(
            "Smooth tracking shot",
            "Slow zoom in",
            "Panoramic sweep",
            "Dynamic orbit",
            "Steady dolly forward"
        )

        val lightingStyles = listOf(
            "Warm golden hour lighting",
            "Professional studio lighting",
            "Soft natural daylight",
            "Dramatic high-contrast",
            "Clean minimalist lighting"
        )

        val musicMoods = listOf(
            "Upbeat inspiring corporate",
            "Calm and professional",
            "Energetic and motivating",
            "Elegant and sophisticated",
            "Warm and inviting"
        )

        return Veo3Prompt(
            title = "$product - $style Video",
            prompt = """
                Create a $duration $style video for $product.
                
                Opening: Steaming cup of premium tea/coffee with product logo overlay.
                Middle: Show product in use - vending machine dispensing, packaging showcase, quality ingredients.
                End: Call to action with contact details.
                
                Style: $style, smooth transitions, professional quality.
                Target audience: B2B business owners, cafes, offices.
            """.trimIndent(),
            style = style,
            duration = duration,
            aspectRatio = aspectRatio,
            cameraMovement = cameraMovements.random(),
            lighting = lightingStyles.random(),
            musicMood = musicMoods.random(),
            textOverlays = listOf(
                "Premium Quality",
                "Best Prices",
                "Pan India Delivery",
                "FSSAI Certified"
            )
        )
    }

    fun getVeo3ReadyPrompt(prompt: Veo3Prompt): String {
        return """
            ${prompt.prompt}
            
            Camera: ${prompt.cameraMovement}
            Lighting: ${prompt.lighting}
            Music: ${prompt.musicMood}
            Aspect Ratio: ${prompt.aspectRatio}
            
            Text overlays: ${prompt.textOverlays.joinToString(", ")}
        """.trimIndent()
    }
}
