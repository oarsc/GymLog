package org.scp.gymlog.model;

import static org.scp.gymlog.util.Constants.ONE_HUNDRED;

import androidx.annotation.NonNull;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.util.Data;

import java.math.BigDecimal;
import java.util.Date;

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
	private Bar bar;
	private String note;
	private Date timestamp;

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
		if (bar != null) {
			entity.barId = bar.getId();
		}
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Bit fromEntity(@NonNull BitEntity entity) {
		id = entity.barId;
		exerciseId = entity.exerciseId;
		trainingId = entity.trainingId;
		note = entity.note;
		timestamp = entity.timestamp;
		reps = entity.reps;
		weight = new Weight(
				BigDecimal.valueOf(entity.totalWeight).divide(ONE_HUNDRED),
				entity.kilos
		);
		if (entity.barId != null) {
			bar = Data.getBar(entity.barId);
		}
		return this;
	}
}
