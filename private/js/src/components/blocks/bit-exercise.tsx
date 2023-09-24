import './bit-exercise.scss';
import { Output, OutputBit, OutputExercise, OutputVariation } from '../../../model/output-format';
import React from 'react';
import Accordion from '../elements/accordion';
import { sort } from '../../utils';
import { VariationBundle, dataObservable, getData, setData } from '../../data-manager';
import { BitTable, BitTableProperties, BitTableState } from './bit-table';

interface BitBlockProperties {
  exerciseId?: number
}

interface BitBlockState {
  exercise?: OutputExercise
  variations: OutputVariation[]
  bitsBundle: BitTrainingBundle[]
  page: number
  maxPages: number
}

interface BitTrainingBundle {
  trainingId: number
  bits: OutputBit[]
}

export default class BitExerciseBlock extends React.Component<BitBlockProperties, BitBlockState> {
  pageSize = 10;

  constructor(props: BitBlockProperties) {
    super(props);

    if (props.exerciseId === undefined) {
      this.state = { bitsBundle: [], page: 0, maxPages: -1, variations: [] }
    } else {
      this.state = this.generateState(props.exerciseId, getData());

      dataObservable().subscribe(data => {
        const exerciseId = this.state.exercise?.exerciseId;
        this.setState({
          exercise: data.exercises.find(ex => ex.exerciseId === exerciseId),
          variations: data.variations.filter(va => va.exerciseId === exerciseId),
        });
      });
    }
  }

  override componentDidUpdate(prevProps: BitBlockProperties) {
    const { exerciseId } = this.props;
    if (prevProps.exerciseId != exerciseId) {

      if (exerciseId === undefined) {
        this.setState({ bitsBundle:[], exercise: undefined });

      } else {
        this.setState(this.generateState(exerciseId, getData()));
        document.getElementById('bit-exercise-block')
          ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }
  }

  generateState(exerciseId: number, data: Output): BitBlockState {
    const exercise = data.exercises
      .find(ex => ex.exerciseId === exerciseId)!;
    const variations = data.variations
      .filter(va => va.exerciseId === exerciseId);

    const bundles = this.reloadBundles(variations);

    return {
      page: 0,
      maxPages: Math.ceil(bundles.length / this.pageSize) - 1,
      bitsBundle: bundles,
      exercise,
      variations
    }
  }

  reloadBundles = (variations: OutputVariation[] = this.state.variations) => {
    const varsIds = variations.map(va => va.variationId);

    return getData().bits
      .filter(bit => varsIds.indexOf(bit.variationId) >= 0)
      .sort(sort(bit => bit.timestamp))
      .sort(sort(bit => bit.trainingId))
      .red((obj, bit) => {
        const lastBundle = obj[obj.length-1];
        if (lastBundle.trainingId === bit.trainingId) {
          lastBundle.bits.push(bit);
        } else {
          obj.push({trainingId: bit.trainingId, bits: [bit]});
        }
      }, [{trainingId: -1, bits: []}] as BitTrainingBundle[])
      .slice(1);
  }

  refreshBitsList = () => this.setState({ bitsBundle: this.reloadBundles(this.state.variations) });

  nextPage = () => {
    const { page, maxPages } = this.state;
    if (page < maxPages) {
      this.setState({ page: page + 1});
    }
  }

  prevPage = () => {
    const { page } = this.state;
    if (page > 0) {
      this.setState({ page: page - 1});
    }
  }

  override render(): JSX.Element {
    const keyBase = (() => {
      const { exercise, variations } = this.state;
      return exercise?.name + variations.map(va => va.name).join('');
    })();

    const startBit = this.state.page * this.pageSize;
    const lastBit = startBit + this.pageSize;

    const paging = (
      <div className='bit-exercise-paging'>
        <button
          className='transparent'
          onClick={this.prevPage}
          disabled={this.state.page <= 0}>
            &lt;
        </button>
        <span>{this.state.page + 1}/{this.state.maxPages + 1}</span>
        <button
          className='transparent'
          onClick={this.nextPage}
          disabled={this.state.page >= this.state.maxPages}>
            &gt;
        </button>
      </div>
    );

    const content = this.state.maxPages < 0? <></> : (
      <>
        { paging }
        {
          this.state.bitsBundle.slice(startBit, lastBit).map(bitBundle =>
            <BitTraining
              key={keyBase + bitBundle.trainingId}
              trainingId={bitBundle.trainingId}
              bits={bitBundle.bits}
              exercise={this.state.exercise!}
              variations={this.state.variations}
              onBitListModified={this.refreshBitsList} />
          )
        }
        { paging }
      </>
    )
    
    return <Accordion id='bit-exercise-block' title='Bits by exercise' collapse={false} >
      { content }
    </Accordion>;
  }
}



interface BitTrainingProperties extends BitTableProperties {
  trainingId: number
  variations: OutputVariation[]
  exercise: OutputExercise
}

class BitTraining extends BitTable<BitTrainingProperties, BitTableState> {

  constructor(props: BitTrainingProperties) {
    super(props);
  }

  override getAvailableVariations(props: BitTrainingProperties): VariationBundle[] {
    return props.variations
      .map(variation => ({
        exercise: props.exercise!,
        variation
      }));
  }

  override updateState(state: BitTableState): BitTableState {
    const variationIds = this.props.variations.map(va => va.variationId);

    const newState = {
      ...state,
      bits: getData().bits
        .filter(bit => bit.trainingId == this.props.trainingId)
        .filter(bit => variationIds.indexOf(bit.variationId) >= 0)
    }

    if (this.props.onBitListModified) {
      this.props.onBitListModified();
    }

    return newState;
  }

  editAll(variationId: number) {
    setData(data => {
      this.props.bits.forEach(bit => {
        const index = data.bits.findIndex(b => b.timestamp == bit.timestamp);
        data.bits[index].variationId = variationId;
        bit.variationId = variationId;
      });
    });

    if (this.props.onBitListModified)
      this.props.onBitListModified();
  }

  override render(): JSX.Element {

    const data = getData()

    const training = data.trainings.find(tr => tr.trainingId == this.props.trainingId);
    const gymName = training == null
      ? ''
      : ` (${data.gyms[training.gymId-1]})`

    return <div className='bit-training'>
      <p>Training #{this.props.trainingId}{gymName}</p>

      {
        this.props.variations
          .sort(sort(va => !va.def))
          .map((variation, idx) =>
            <button className='gray' key={idx} onClick={ this.editAll.bind(this, variation.variationId)}>
              { variation.def? this.props.exercise.name : variation.name }
            </button>
          )
      }

      { super.render() }
    </div>;
  }
}
