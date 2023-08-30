import './bit.scss';
import { Output, OutputBit, OutputExercise, OutputVariation } from '../../../model/output-format';
import React from 'react';
import Accordion from '../elements/accordion';
import Input from '../elements/custom-input';
import {  VariationBundle, dataObservable, getData, getDataSortedVariations } from '../../data-manager';
import { BitTable, BitTableProperties, BitTableState } from './bit-table';
import { getElementById } from '../../../_dom/dom-utils';

const FIRST_TRAINING_TIME = 1514368342000;

interface BitBlockProperties {
}

interface BitBlockState {
  date: Date
  trainingId: number
  bits: OutputBit[]
}

export default class BitBlock extends React.Component<BitBlockProperties, BitBlockState> {

  constructor(props: BitBlockProperties) {
    super(props);

    const date = new Date();
    date.setUTCHours(0);
    date.setUTCMinutes(0);
    date.setUTCSeconds(0);
    date.setUTCMilliseconds(0);

    dataObservable().subscribe(data =>
      this.setStateDate(this.state.date, data)
    );

    const bits = this.filterBits(date, getData());

    this.state = {
      date,
      trainingId: bits.length && bits[0].trainingId || 0,
      bits
    }
  }

  filterBits(date: Date, data: Output): OutputBit[] {
    const startDate = date.getTime();
    const endDate = (date => {
        date.setDate(date.getDate()+1);
        return date.getTime();
      })(new Date(startDate));

    return data.bits
      .filter(bit => startDate < bit.timestamp && bit.timestamp < endDate);
  }

  prevDay = () => {
    const date = new Date(this.state.date);
    do {
      date.setDate(date.getDate() - 1);
    } while (!this.setStateDate(date));

    getElementById('bits-frame')?.scrollTo(0, 0);
  }

  nextDay = () => {
    const date = new Date(this.state.date);
    do {
      date.setDate(date.getDate() + 1);
    } while (!this.setStateDate(date));

    getElementById('bits-frame')?.scrollTo(0, 0);
  }

  setFilterState = (state: BitBlockState) => {
    this.setStateDate(state.date);
  }

  setFilterTrainingState = (aaa: any) => {
    const input = aaa.target as HTMLInputElement;

    const trainingId = parseInt(input.value = input.value.replace(/[^0-9]/g, ''));

    const bit = getData().bits.find(bit => bit.trainingId == trainingId);
    
    if (bit) {
      const date = new Date(bit.timestamp);
      date.setUTCHours(0);
      date.setUTCMinutes(0);
      date.setUTCSeconds(0);
      date.setUTCMilliseconds(0);
      this.setStateDate(date);
    }
  }

  setStateDate(date: Date, data: Output = getData()): boolean {
    const bits = this.filterBits(date, data);

    if (bits?.length || date.getTime() > new Date().getTime() || date.getTime() < FIRST_TRAINING_TIME) {
      this.setState({
        date,
        trainingId: bits.length && bits[0].trainingId || 0,
        bits
      });
      return true;
    }
    return false;
  }

  override render(): JSX.Element {
    const { bits } = this.state;
    const trainings = bits
      .map(bit => bit.trainingId)
      .filter((value, idx, array) => array.indexOf(value) === idx)
      .sort();

    return <Accordion id='bit-block' title='Bits by day' collapse={false} block={true}>
      <div id='id-filter'>
        <Input
          id='bit-date'
          type='date'
          data={this.state}
          dataKey='date'
          useDate={true}
          linkValue={true}
          onChange={this.setFilterState} />
        <button onClick={this.prevDay}>&lt;</button>
        <button onClick={this.nextDay}>&gt;</button>
        <Input
          id='bit-training'
          type='number'
          data={this.state}
          dataKey='trainingId'
          linkValue={false}
          onBlur={this.setFilterTrainingState} />
      </div>
      <div id='bits-frame'>
        <div id='bits'>
          {
            trainings.map(trainingId => {
              const trainingBits = bits.filter(bit => bit.trainingId == trainingId);
              return <BitTraining
                key={trainingId + trainingBits.map(bit => bit.variationId).join('')}
                trainingId={trainingId}
                bits={trainingBits} />
            })
          }
        </div>
      </div>
    </Accordion>;
  }
}



interface BitTrainingProperties {
  trainingId: number
  bits: OutputBit[]
}

interface BitTrainingState {
  bitsByVariation: BitGroup[]

}

interface BitGroup {
  bits: OutputBit[]
  variationId: number
}

class BitTraining extends React.Component<BitTrainingProperties, BitTrainingState> {
  constructor(props: BitTrainingProperties) {
    super(props);

    this.state = {
      bitsByVariation: this.getBitsByVariation()
    }
  }

  getBitsByVariation(): BitGroup[] {
    return this.props.bits.slice(1).red((acc, bit) => {
      const lastGroup = acc.slice(-1)[0];

      if (lastGroup.variationId === bit.variationId) {
        lastGroup.bits.push(bit);
      } else {
        acc.push({
          bits:[bit],
          variationId: bit.variationId
        });
      }
    }, [{
      bits:[this.props.bits[0]],
      variationId: this.props.bits[0].variationId
    }] as BitGroup[]);
  }

  override render(): JSX.Element {
    return <div className='bit-training'>
      <p>Training #{this.props.trainingId}</p>
      <div className='bit-variations'>
        {
          this.state.bitsByVariation.map((bitGroup, idx) =>
            <BitVariation
              key={`${idx}_${bitGroup.variationId}`}
              trainingId={this.props.trainingId}
              variationId={bitGroup.variationId}
              bits={bitGroup.bits} />
          )
        }
      </div>
    </div>;
  }
}

interface BitVariationProperties extends BitTableProperties {
  trainingId: number
  variationId: number
}

interface BitVariationState extends BitTableState {
  variation: OutputVariation
  exercise: OutputExercise
}

class BitVariation extends BitTable<BitVariationProperties, BitVariationState> {
  constructor(props: BitVariationProperties) {
    super(props);

    const data = getData();

    const variation = data.variations.find(va => va.variationId == props.variationId)!;
    this.state = {
      ...this.state,
      variation,
      exercise: data.exercises.find(ex => ex.exerciseId == variation.exerciseId)!
    }
  }

  override getAvailableVariations(_: BitTrainingProperties): VariationBundle[] {
    return getDataSortedVariations();
  }

  override updateState(state: BitVariationState): BitVariationState {
    return {
      ...state,
      bits: getData().bits
        .filter(bit => bit.trainingId == this.props.trainingId)
        .filter(bit => bit.variationId == this.props.variationId)
    };
  }

  override render(): JSX.Element {
    const { exercise, variation } = this.state;

    return <div className='bit-variation'>
      <p>
        <span className='bit-exertise-name'>{ exercise.name }</span>&nbsp;
        <span className='bit-variation-name'>{ variation.name }</span>
      </p>
      { super.render() }
    </div>;
  }
}
