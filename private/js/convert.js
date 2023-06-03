import { ajax, getElementById, querySelectorAll } from './_dom/dom-utils';

async function init() {
	const content = await ajax('./read').then(res => res.json());

	querySelectorAll('input,button').forEach(element => element.disabled = false);

	(button => {
		const blob = generateJsonBlobFromText(content);
		button.onclick = _ => download(blob, 'output.json');
	})(getElementById('original'));


	(button => {
		const { exercises, variations, bits } = content;

		const exercisesMap = exercises.reduce((map, exercise) => {
			map[exercise.exerciseId] = exercise;
			return map;
		},{});

		const variationMap = variations.reduce((map, variation) => {
			map[variation.variationId] = variation;
			return map;
		},{});

		bits.forEach(bit => {
			const variation = variationMap[bit.variationId];
			const exercise = exercisesMap[variation.exerciseId];

			bit.exercise = exercise.name;
			if (!variation.def) {
				bit.variation = variation.name;
			}
			delete bit.variationId;
		});

		const blob = generateJsonBlobFromText(content);
		button.onclick = _ => download(blob, 'output.work.json');
	})(getElementById('work'));


	getElementById('convert').onclick = _ => {
		const file = getElementById('input').files[0];

		var reader = new FileReader();
		reader.readAsText(file, 'UTF-8');
		reader.onerror = _ => alert('error reading file');
		reader.onload = evt => {
			const json = JSON.parse(evt.target.result);

			const { exercises, variations, bits } = json;

			const exercisesMap = exercises.reduce((map, {name, exerciseId}) => {
				map[name] = exerciseId;
				return map;
			},{});

			const variationDefaults = exercises.reduce((map, exercise) => {
				map[exercise.exerciseId] = variations
					.filter(variation => variation.def && variation.exerciseId == exercise.exerciseId)[0];
				return map;
			},{});

			const undefinedDefaultVariations = Object.entries(variationDefaults).filter(e => !e[1]).map(e => e[0]);
			if (undefinedDefaultVariations.length > 0) {
				throw 'Some exercises misses default variations: '+undefinedDefaultVariations.join(',');
			}

			let maxVariationId = variations
				.map(variation => variation.variationId)
				.reduce((max, val) => val>max?val:max,0);

			bits.forEach(bit => {
				const exerciseId = exercisesMap[bit.exercise];

				if (!bit.variation) {
					bit.variationId = variationDefaults[exerciseId].variationId;

				} else {
					bit.variationId = variations
						.filter(variation => !variation.def)
						.filter(variation => variation.exerciseId == exerciseId)
						.filter(variation => variation.name == bit.variation)
						.map(variation => variation.variationId)[0];

					if (bit.variationId == undefined) {
						// create variation:
						const defaultVariation = variationDefaults[exerciseId];

						variations.push({
							def: false,
							exerciseId: exerciseId,
							lastRestTime: defaultVariation.lastRestTime,
							lastStep: defaultVariation.lastStep,
							lastWeightSpec: defaultVariation.lastWeightSpec,
							name: bit.variation,
							type: defaultVariation.type,
							variationId: ++maxVariationId,
						});

						bit.variationId = maxVariationId;
					}
				}
				delete bit.exercise;
				delete bit.variation;
			});

			const bitsVariationLess = bits.filter(bit => !bit.variationId);
			if (bitsVariationLess.length > 0) {
				console.log(bitsVariationLess);
				throw 'Bits without variationsId';
			}

			const blob = generateJsonBlobFromText(json);
			download(blob, 'output.new.json');
		}
	}
}

function generateJsonBlobFromText(text) {
	return new Blob([JSON.stringify(text)], {type: 'application/json'});
}


function download(blob, filename) {
    const url  = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = filename;

    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
}

init();