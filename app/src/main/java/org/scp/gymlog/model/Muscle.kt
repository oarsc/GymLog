package org.scp.gymlog.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.Setter
import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.MuscleEntity

class Muscle(
	val id: Int,
	@field:StringRes val text: Int,
	@field:DrawableRes val icon: Int,
	@field:ColorRes val color: Int) : EntityMappable<MuscleEntity?> {

	override fun toEntity(): MuscleEntity {
		val entity = MuscleEntity()
		entity.muscleId = id
		return entity
	}
}
