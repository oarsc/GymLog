import './exercise-muscle-select.scss';
import React from 'react';
import MuscleSelect from './muscle-select';
import { MUSCLES, Muscle } from '../../../model/muscle';
import { OutputExercise, OutputMuscleRelation } from '../../../model/output-format';

interface ExerciseMuscleSelectorProperties  {
  relations: OutputMuscleRelation[]
  exercise: OutputExercise
  onChange: (muscles: Muscle[]) => void
}

interface ExerciseMuscleSelectorState {
  panelVisible: boolean
  selection: Muscle[]
}

export default class ExerciseMuscleSelect extends React.Component<ExerciseMuscleSelectorProperties, ExerciseMuscleSelectorState> {

  constructor(props: ExerciseMuscleSelectorProperties) {
    super(props);

    this.state = {
      panelVisible: false,
      selection: props.relations
        .filter(rel => props.exercise.exerciseId == rel.exerciseId)
        .map(rel => this.findMuscle(rel.muscleId))
    };

  }

  findMuscle(id: number): Muscle {
    return MUSCLES.find(muscle => muscle.id === id)!;
  }

  updateMuscle(idx: number, muscleTmp: Muscle | undefined) {
    const muscle = this.findMuscle(muscleTmp!.id);
    const selection = [...this.state.selection];
    selection[idx] = muscle;

    this.setState({ selection });
    this.props.onChange(selection);
  }

  removeMuscle = (idx: number) => {
    const selection = [ ...this.state.selection ];
    selection.splice(idx, 1);

    this.setState({ selection });
    this.props.onChange(selection);
  }

  addMuscle = () => {
    const selection = [
      ...this.state.selection,
      MUSCLES[0]
    ]
    this.setState({ selection });
    this.props.onChange(selection);
  }

  override render(): JSX.Element {
    return (
      <div className='muscle-selector'>
        {
          this.state.selection.map((muscle, idx) => 
            <div key={`${idx} ${muscle.id}`}>
              <button className='transparent' onClick={this.removeMuscle.bind(this, idx)}>−</button>
              <MuscleSelect
                value={muscle}
                onChange={this.updateMuscle.bind(this, idx)} />
            </div>
          )
        }
        <button className='transparent' onClick={this.addMuscle}>+</button>
      </div>
    );
  }
}