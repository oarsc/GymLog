package org.scp.gymlog.model;

import static org.scp.gymlog.util.Constants.ONE_HUNDRED;

import androidx.annotation.NonNull;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.BitEntity;

import java.math.BigDecimal;
import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
public class Bit implements EntityMapped<BitEntity> {
	private int id;
	private int exerciseId;
	private int trainingId;
	private int reps;
	private Weight weight;
	private String note;
	private Calendar timestamp;

	private int set; // used in logRecyclerViewAdapter

	@Override
	public BitEntity toEntity() {
		BitEntity entity = new BitEntity();
		entity.bitId = id;
		entity.exerciseId = exerciseId;
		entity.trainingId = trainingId;
		entity.note = note;
		entity.timestamp = timestamp;
		entity.reps = reps;
		entity.totalWeight = weight.getValue().multiply(ONE_HUNDRED).intValue();
		entity.kilos = weight.isInternationalSystem();
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Bit fromEntity(@NonNull BitEntity entity) {
		id = entity.bitId;
		exerciseId = entity.exerciseId;
		trainingId = entity.trainingId;
		note = entity.note;
		timestamp = entity.timestamp;
		reps = entity.reps;
		weight = new Weight(
				BigDecimal.valueOf(entity.totalWeight).divide(ONE_HUNDRED),
				entity.kilos
		);
		return this;
	}
}
