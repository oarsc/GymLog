import './muscle-select.scss';
import React from 'react';
import Select from './custom-select';
import { MUSCLES, Muscle } from '../../../model/muscle';

interface MuscleSelectorProperties extends Omit<React.HTMLProps<HTMLSelectElement>, 'value' | 'onChange' | 'defaultValue'> {
  value?: number | Muscle
  onChange?: (muscles: Muscle | undefined) => void
  noneText?: string
}

interface MuscleSelectorState {
  selected?: Muscle
}

export default class MuscleSelect extends React.Component<MuscleSelectorProperties, MuscleSelectorState> {

  constructor(props: MuscleSelectorProperties) {
    super(props);

    const muscle = !props.value
      ? (props.noneText? undefined : MUSCLES[0])
      : (typeof props.value == 'number'? this.findMuscle(props.value) : props.value);

    this.state = {
      selected: muscle
    };
  }

  findMuscle(id?: number): Muscle | undefined {
    const muscle = MUSCLES.find(muscle => muscle.id === id);
    return muscle ?? (this.props.noneText? undefined : MUSCLES[0]);
  }

  updateMuscle = (muscleTmp: Muscle) => {
    const muscle = this.findMuscle(muscleTmp.id);
    this.setState({ selected: muscle });
    
    if (this.props.onChange) {
      this.props.onChange(muscle);
    }
  }

  override render(): JSX.Element {
    const muscle = this.state.selected ?? { id: 0, color: '#444', name: '' };

    return (
      <Select
        className={this.props.className + ' cstm-muscle-select'}
        type='number'
        data={muscle}
        dataKey='id'
        style={{boxShadow: `2px 2px 0 0 ${muscle.color}`, borderColor: muscle.color}}
        onChange={this.updateMuscle} >
        {
          this.props.noneText &&
          <option value='0'>{ this.props.noneText }</option>
        }
        {
          MUSCLES.map(m =>
            <option key={m.id} value={m.id}>{m.name}</option>
          )
        }
      </Select>
    );
  }
}