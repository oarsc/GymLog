import { Output, OutputExercise, OutputVariation } from '../model/output-format';
import { Observable, Subject } from 'rxjs';
import { sort } from './utils';

export interface VariationBundle {
  variation: OutputVariation
  exercise: OutputExercise
}

const $data = new Subject<Output>();
let _data: Output;

let sortedVariations: VariationBundle[] = [];
let sortedExercises: OutputExercise[] = [];

function updateCachedData(data: Output) {
  sortedVariations = [ ...data.variations ]
    .sort(sort(variation => variation.name))
    .map(variation => ({
      variation,
      exercise: data.exercises.find(ex => ex.exerciseId == variation.exerciseId)!
    }))
    .sort(sort(({exercise}) => exercise.name));

  sortedExercises = [ ...data.exercises ]
    .sort(sort(exercise => exercise.name));
}

export function setData(callback: (data: Output) => Output | void): Output {
  const result = callback(_data);
  if (result) {
    _data = result;
  }
  return _data;
}

export function dataObservable(): Observable<Output> {
  return $data.asObservable();
}

export function getData(): Output {
  return _data;
}

export function forceDataReload() {
  updateCachedData(_data);
  $data.next(_data);
}

export function getDataSortedVariations(forceRecalc = false): VariationBundle[] {
  if (forceRecalc) {
    updateCachedData(_data);
  }
  return sortedVariations;
}

export function getDataSortedExercises(forceRecalc = false): OutputExercise[] {
  if (forceRecalc) {
    updateCachedData(_data);
  }
  return sortedExercises;
}

