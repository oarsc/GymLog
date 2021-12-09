package org.scp.gymlog.model;

import static org.scp.gymlog.util.Constants.ONE_HUNDRED;

import androidx.annotation.NonNull;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.BarEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
public class Bar implements EntityMapped<BarEntity> {
	private final List<Muscle> belongingMuscles = new ArrayList<>();
	private int id;
	private Weight weight;

	@Override
	public BarEntity toEntity() {
		BarEntity entity = new BarEntity();
		entity.barId = id;
		entity.weight = weight.getValue().multiply(ONE_HUNDRED).intValue();
		entity.internationalSystem = weight.isInternationalSystem();
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Bar fromEntity(@NonNull BarEntity entity) {
		id = entity.barId;
		weight = new Weight(
				BigDecimal.valueOf(entity.weight).divide(ONE_HUNDRED),
				entity.internationalSystem
		);
		return this;
	}
}
