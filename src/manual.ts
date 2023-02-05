#!/usr/bin/env node

import * as fs from 'fs';

interface OutputType {
  prefs: {
   exercisesSortLastUsed?: 'last_used' | 'alphabetically',
   restTime?: number,
   nightTheme: boolean,
   internationalSystem: boolean,
   gym: number,
   conversionExactValue: boolean,
   conversionStep: string
  },
  gyms: Gym[],
  exercises: Exercise[],
  variations: Variation[],
  primaries: MuscleLink[],
  secondaries: MuscleLink[],
  trainigs: Training[],
  bits: Bit[]
}

type Gym = string;

interface Exercise {
  exerciseId: number,
  image: string,
  name: string
}

interface Variation {
  def: boolean,
  exerciseId: number,
  gymId?: number,
  gymRelation: 0|1|2, //NO_RELATION, INDIVIDUAL_RELATION, STRICT_RELATION
  lastRestTime: number,
  lastStep: number,
  lastWeightSpec: number,
  name: string,
  type: number,
  variationId: number,
}

interface MuscleLink {
  exerciseId: number,
  muscleId: number
 }

interface Training {
  end: number,
  start: number,
  trainingId: number
 }

interface Bit {
  gymId: number,
  instant: boolean,
  note?: string,
  reps: number,
  timestamp: number,
  totalWeight: number,
  trainingId: number,
  variationId: number
}

interface NewVariation {
  noteName: string,
  variationName?: string,
  gymRelation?: 0|1|2,
  gymId?: number,
}

const outputJson = JSON.parse(fs.readFileSync('../bak/output.json', 'utf-8')) as OutputType;


((exerciseName: string, newVariations: NewVariation[]) => {
  const exercise = outputJson.exercises
    .find(exercise => exercise.name === exerciseName);

  if (!exercise) throw new Error(`Exercise "${exerciseName}" not found`);

  const variations = outputJson.variations
    .filter(variation => variation.exerciseId === exercise.exerciseId)
  const defaultVariation = variations.find(variation => variation.def)!;

  const initialMaxVariationId = outputJson.variations
  .reduce((max, variation) => variation.variationId > max? variation.variationId : max, 0);
  let maxVariationId = initialMaxVariationId;

  const newVariationsMap = newVariations.reduce((map, newVar) => {
    if (!newVar.variationName) {
      map[newVar.noteName] = defaultVariation;
    } else {
      const foundVariation = variations.find(variation => variation.name === newVar.variationName);
      if (foundVariation) {
        map[newVar.noteName] = foundVariation;
      } else {
        const newVariation: Variation = {
          ...defaultVariation,
          name: newVar.variationName,
          variationId: ++maxVariationId,
          def: false,
          gymRelation: newVar.gymRelation ?? defaultVariation.gymRelation
        }

        if (newVariation.gymRelation == 2) {
          newVariation.gymId = newVar.gymId;
        }
        variations.push(newVariation);
        map[newVar.noteName] = newVariation;
      }
    }
    return map;
  }, {} as {[key: string]: Variation} );

  variations
    .filter(variation => variation.variationId > initialMaxVariationId)
    .forEach(variation => outputJson.variations.push(variation));

  const variationsId = variations
    .map(variation => variation.variationId)
    .filter(id => id <= initialMaxVariationId);

  let lastTraining = -1;
  let lastVariation: Variation | undefined = undefined;

  outputJson.bits
    .filter(bit => variationsId.indexOf(bit.variationId) >= 0)
    .map(bit => {
      if (lastTraining !== bit.trainingId) {
        lastTraining = -1;
        lastVariation = undefined;
      }

      const found = Object.keys(newVariationsMap).find(noteName =>
        (bit.note ?? '').indexOf(noteName) == 0
      )

      if (found) {
        bit.note = bit.note?.replace(found, '').trim();
        if (!bit.note) {
          delete bit.note
        }

        lastVariation = newVariationsMap[found];
        bit.variationId = lastVariation.variationId;

      } else if (lastTraining < 0 || !lastVariation) {
        lastVariation = defaultVariation;
        bit.variationId = lastVariation.variationId;

      } else {
        bit.variationId = lastVariation.variationId;
      }

      lastTraining = bit.trainingId;
      return bit;
    });


  const out = JSON.stringify(outputJson, null, 1);
  fs.writeFileSync('../bak/output_2.json', out, 'utf-8')

})('Pulley Machine Rope Triceps Press (standing)', [
  { noteName: '2m8p' },
  { noteName: '2m 8p' },
  { noteName: '8p' },
  { noteName: '2m2p', variationName: '2 pulleys', gymRelation: 2 , gymId: 1 },
  { noteName: '2m 2p', variationName: '2 pulleys', gymRelation: 2 , gymId: 1 },
  { noteName: '2p', variationName: '2 pulleys', gymRelation: 2 , gymId: 1 },
  { noteName: '1m8p', variationName: 'Single hand', gymRelation: 1 },
  { noteName: '1m 8p', variationName: 'Single hand', gymRelation: 1 },
  { noteName: '1m2p', variationName: 'Single hand, 2 pulleys', gymRelation: 2 , gymId: 1 },
  { noteName: '1m 2p', variationName: 'Single hand, 2 pulleys', gymRelation: 2 , gymId: 1 },
]);

//gymRelation: 0|1|2, //NO_RELATION, INDIVIDUAL_RELATION, STRICT_RELATION