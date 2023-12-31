package com.project.audiorecording.audiorecordingserver

import com.project.audiorecording.audiorecordingserver.controller.*
import com.project.audiorecording.audiorecordingserver.domain.dto.*
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import com.project.audiorecording.audiorecordingserver.utils.ClassInspector
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.ResponseEntity
import java.time.Duration
import java.util.*
import kotlin.system.exitProcess

val classInspector: ClassInspector = ClassInspector()

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.project.audiorecording.audiorecordingserver.repository"])
class AudioRecordingServerApplication(
    override val discController: DiscController,
    override val trackController: TrackController,
    override val rockCompositionController: RockCompositionController,
    override val popCompositionController: PopCompositionController,
    override val classicalCompositionController: ClassicalCompositionController,
) : App {

    override fun start() {
        var running = true

        while (running) {
            println("=== Audio Recording Server ===")
            println("1. Create a new disc")
            println("2. Add a track to a disc")
            println("3. List all discs")
            println("4. List tracks of a disc")
            println("5. Calculate the duration of a disc")
            println("6. Sort tracks by style")
            println("7. Find tracks by length range")
            println("8. List all tracks")
            println("9. Remove a track")
            println("10. Remove a disc")
            println("11. Exit")

            val input = readlnOrNull() ?: ""
            when (input) {
                "1" -> createDisc()
                "2" -> addTrackToDisc()
                "3" -> listAllDiscs()
                "4" -> listTracksOfDisc()
                "5" -> calculateDurationOfDisc()
                "6" -> sortTracksByStyle()
                "7" -> findTracksByLengthRange()
                "8" -> listAllTracks()
                "9" -> removeTrack()
                "10" -> removeDisc()
                "11" -> running = false
                else -> println("Invalid input")
            }
        }

        exitProcess(0)
    }

    override fun createDisc() {
        println("=== Create a New Disc ===")
        println("Enter disc name:")
        val name = readlnOrNull() ?: ""
        println("Enter the number of tracks for the disc (you can add tracks later):")
        val trackNumber = readlnOrNull()?.toInt() ?: 0

        var disc = DiscDto(
            name = name,
            trackNumber = trackNumber,
            totalDuration = Duration.ZERO,
        )

        val discResponse = discController.create(disc)
        printOperationResult(discResponse)
        disc = discResponse.body!!

        for (i in 1..trackNumber) {
            addTrackToDisc(disc.id!!)
        }
    }

    private fun <T> printOperationResult(result: ResponseEntity<T>) {
        if (result.statusCode.is2xxSuccessful) {
            println("Operation successful")
            println(result.toString())

        } else {
            println("Operation failed")
        }
    }

    override fun addTrackToDisc() {
        println("=== Add a Track to a Disc ===")
        println("Enter disc id:")
        val discId = readlnOrNull() ?: ""
        try {
            addTrackToDisc(UUID.fromString(discId))
        } catch (e: Exception) {
            println("Invalid disc id")
        }
    }

    @Throws(Exception::class)
    private fun addTrackToDisc(discId: UUID) {
        println("Enter track type")
        println("1. Rock")
        println("2. Pop")
        println("3. Classical")

        val disc = discController.getOne(discId).body!!
        val input = readlnOrNull() ?: ""
        when (input) {
            "1" -> {
                val rockComposition = getRockComposition(disc)
                printOperationResult(rockCompositionController.create(rockComposition))
            }
            "2" -> {
                val popComposition = getPopComposition(disc)
                printOperationResult(popCompositionController.create(popComposition))
            }
            "3" -> {
                val classicalComposition = getClassicalComposition(disc)
                printOperationResult(classicalCompositionController.create(classicalComposition))
            }
            else -> println("Invalid input")
        }
    }

    private fun getTrack(disc: DiscDto): TrackDto {
        println("Enter track title")
        val title = readlnOrNull() ?: ""
        println("Enter track duration (in seconds)")
        val duration: Duration = Duration.ofSeconds(readlnOrNull()?.toLong() ?: 0)
        println("Enter track author")
        val author = readlnOrNull() ?: ""

        return TrackDto(
            title = title,
            duration = duration,
            author = author,
            disc = disc
        )
    }

    private fun getRockComposition(disc: DiscDto): RockCompositionDto {
        val track = getTrack(disc)
        println("Enter rock composition style")
        val style = readlnOrNull() ?: ""

        return RockCompositionDto(
            track = track,
            style = style
        )
    }

    private fun getPopComposition(disc: DiscDto): PopCompositionDto {
        val track = getTrack(disc)
        println("Enter pop composition genre")
        val genre = readlnOrNull() ?: ""
        println("Enter pop composition popularity")
        val popularity = readlnOrNull()?.toInt() ?: 0

        return PopCompositionDto(
            track = track,
            genre = genre,
            popularity = popularity
        )
    }

    private fun getClassicalComposition(disc: DiscDto): ClassicalCompositionDto {
        val track = getTrack(disc)
        println("Enter classical composition epoch")
        val epoch = readlnOrNull() ?: ""

        return ClassicalCompositionDto(
            track = track,
            epoch = epoch
        )
    }

    override fun listAllDiscs() {
        val discs = discController.getAll().body!!
        discs.forEach { disc ->
            classInspector.printObjectProperties(disc)
        }
    }

    override fun listTracksOfDisc() {
        println("=== List Tracks of a Disc ===")
        println("Enter disc id:")
        val discId = readlnOrNull() ?: ""
        try {
            val tracks = discController.getAllTracks(UUID.fromString(discId)).body!!
            tracks.forEach { track ->
                classInspector.printObjectProperties(track)
            }
        } catch (e: Exception) {
            println("Invalid disc id")
        }
    }

    override fun calculateDurationOfDisc() {
        println("=== Calculate Duration of a Disc ===")
        println("Enter disc id:")
        val discId = readlnOrNull() ?: ""
        try {
            val totalDuration = discController.calculateDuration(UUID.fromString(discId)).toString()
            println("Disc total duration: $totalDuration")
        } catch (e: Exception) {
            println("Invalid disc id")
        }
    }

    override fun sortTracksByStyle() {
        println("=== Sort Tracks by Style ===")
        println("Enter disc id:")
        val discId = readlnOrNull() ?: ""
        try {
            discController.getSortedByStyle(UUID.fromString(discId)).forEach { track ->
                classInspector.printObjectProperties(track)
            }
        } catch (e: Exception) {
            println("Invalid disc id")
        }
    }

    override fun findTracksByLengthRange() {
        println("=== Find Tracks by Length Range ===")
        println("Enter disc id:")
        val discId = readlnOrNull() ?: ""
        try {
            println("Enter min duration (in seconds)")
            val minDuration = Duration.ofSeconds(readlnOrNull()?.toLong() ?: 0)
            println("Enter max duration (in seconds)")
            val maxDuration = Duration.ofSeconds(readlnOrNull()?.toLong() ?: 0)
            discController.findSongsByLength(UUID.fromString(discId), minDuration, maxDuration).forEach { track ->
                classInspector.printObjectProperties(track)
            }
        } catch (e: Exception) {
            println("Invalid disc id")
        }
    }

    override fun listAllTracks() {
        val tracks = trackController.getAll().body!!
        tracks.forEach { track ->
            classInspector.printObjectProperties(track)
        }
    }

    override fun removeTrack() {
        println("=== Remove a Track ===")
        println("Enter track id:")
        val trackId = readlnOrNull() ?: ""
        try {
            printOperationResult(trackController.delete(UUID.fromString(trackId)))
        } catch (e: Exception) {
            println("Invalid track id")
        }
    }

    override fun removeDisc() {
        println("=== Remove a Disc ===")
        println("Enter disc id:")
        val discId = readlnOrNull() ?: ""
        try {
            discController.delete(UUID.fromString(discId))
        } catch (e: Exception) {
            println("Invalid disc id")
        }
    }

    @Autowired
    lateinit var app: App

    @PostConstruct
    fun onApplicationStart() {
        app.start()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(AudioRecordingServerApplication::class.java, *args)
}
