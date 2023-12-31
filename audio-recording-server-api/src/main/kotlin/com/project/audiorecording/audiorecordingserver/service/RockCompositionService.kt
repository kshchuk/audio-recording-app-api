package com.project.audiorecording.audiorecordingserver.service

import com.project.audiorecording.audiorecordingserver.domain.dto.RockCompositionDto
import com.project.audiorecording.audiorecordingserver.domain.entity.RockComposition
import com.project.audiorecording.audiorecordingserver.mapper.DiscMapper
import com.project.audiorecording.audiorecordingserver.mapper.RockCompositionMapper
import com.project.audiorecording.audiorecordingserver.repository.RockCompositionRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service for {@link RockComposition}
 */
@Service
@RequiredArgsConstructor
class RockCompositionService(
    private val rockCompositionRepository: RockCompositionRepository,
    private val rockCompositionMapper: RockCompositionMapper,
    private val trackService: TrackService,
    private val discMapper: DiscMapper
) : CrudService<RockComposition, RockCompositionDto, UUID> {
    override fun create(dto: RockCompositionDto): RockCompositionDto {
        val rockComposition = getEntity(dto)
        val rockDto =  rockCompositionMapper.toDto(rockCompositionRepository.save(rockComposition))
        rockDto.disc = discMapper.toDto(rockComposition.disc!!)
        trackService.updateDisc(rockComposition.disc?.id!!)
        return rockDto
    }

    override fun read(id: UUID): RockCompositionDto {
        return rockCompositionMapper.toDto(requireOne(id))
    }

    override fun update(dto: RockCompositionDto): RockCompositionDto {
        requireOne(dto.id!!)
        val updatedRockComposition = getEntity(dto)
        val rockDto = rockCompositionMapper.toDto(rockCompositionRepository.save(updatedRockComposition))
        trackService.updateDisc(updatedRockComposition.disc!!.id!!)
        rockDto.disc = discMapper.toDto(updatedRockComposition.disc!!)
        return rockDto
    }

    override fun delete(id: UUID) {
        val rockDto = read(id)
        rockCompositionRepository.deleteById(id)
        trackService.updateDisc(rockDto.disc!!.id!!)
    }

    override fun getAll(): List<RockCompositionDto> {
        return rockCompositionMapper.ToDtoList(rockCompositionRepository.findAll())!!
    }

    override fun requireOne(id: UUID): RockComposition {
        return rockCompositionRepository.findById(id).orElseThrow(
            { throw RuntimeException("RockComposition with id $id not found") }
        )
    }

    private fun getEntity(dto: RockCompositionDto): RockComposition {
        val track = trackService.getEntity(dto)
        val rockComposition = RockComposition(
            style = dto.style,
        )
        rockComposition.id = track.id
        rockComposition.title = track.title
        rockComposition.author = track.author
        rockComposition.duration = track.duration
        rockComposition.disc = track.disc

        return rockComposition
    }
}