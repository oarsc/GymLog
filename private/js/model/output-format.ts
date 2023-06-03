export interface Output {
  prefs: OutputPreferences,
  gyms: string[],
  exercises: OutputExercise[],
  variations: OutputVariation[],
  primaries: OutputMuscleRelation[],
  secondaries: OutputMuscleRelation[],
  trainings: OutputTraining[];
  bits: OutputBit[],
}

export interface OutputPreferences {
  exercisesSortLastUsed?: 'alphabetically' | 'last_used',
  restTime?: string,
  nightTheme?: boolean,
  internationalSystem?: boolean,
  gym?: number,
  conversionExactValue?: boolean,
  conversionStep?: string,
 }

export interface OutputExercise {
  exerciseId: number,
  image: string,
  name: string,
}

export interface OutputVariation {
  def: boolean,
  exerciseId: number,
  gymId?: number,
  gymRelation: RelationType,
  lastBarId?: BarId,
  lastRestTime: number,
  lastStep: number,
  lastWeightSpec: WeightSpec,
  name: string,
  type: VariationType,
  variationId: number,
 }

 export type VariationType = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7;
 export type RelationType = 0 | 1 | 2;
 export type BarId = 1 | 2 | 3 | 4 | 5 | 6;
 export type WeightSpec = 0 | 1 | 2;

 export interface OutputMuscleRelation {
  exerciseId: number,
  muscleId: number,
 }

 export interface OutputTraining {
  end: number,
  start: number,
  trainingId: number,
 }

 export interface OutputBit {
  gymId: number,
  instant?: boolean,
  note?: string,
  reps: number,
  superSet?: number,
  timestamp: number,
  totalWeight: number,
  trainingId: number,
  variationId: number,
 }