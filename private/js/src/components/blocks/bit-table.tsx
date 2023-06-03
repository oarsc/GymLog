import { OutputBit } from '../../../model/output-format';
import React from 'react';
import Input from '../elements/custom-input';
import Select from '../elements/custom-select';
import { VariationBundle, getData, setData } from '../../data-manager';

export interface BitTableProperties {
  bits: OutputBit[]
  onBitListModified?: () => void
}

export interface BitTableState {
  bits: OutputBit[]
  availableVariations: VariationBundle[]
}

export abstract class BitTable<P extends BitTableProperties = BitTableProperties, S extends BitTableState = BitTableState> extends React.Component<P, S> {
  constructor(props: P) {
    super(props);

    this.state = {
      bits: props.bits,
      availableVariations: this.getAvailableVariations(props),
    } as S;
  }

  abstract getAvailableVariations(props: P): VariationBundle[];
  abstract updateState(state: S): S;

  singleBitModified(index: number, bit: OutputBit) {
    this.setState({
      bits: this.state.bits.map((b, idx) => idx === index? bit : b)
    });

    if (this.props.onBitListModified) {
      this.props.onBitListModified();
    }
  }

  reloadList = () => {
    this.setState(
      this.updateState(this.state)
    );
  }

  override render(): JSX.Element {
    return <table>
      <thead>
        <tr>
          <th>Instant</th>
          <th>Gym</th>
          <th>Super Set</th>
          <th>Reps</th>
          <th>Weight</th>
          <th>Note</th>
          <th>Variation</th>
          <th>Timestamp</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody className='bits'>
        {
        this.state.bits.map((bit, idx) =>
          <BitRow
            key={`${idx}_${bit.timestamp}_${bit.variationId}`}
            bit={bit}
            availableVariations={this.state.availableVariations}
            onBitModified={this.singleBitModified.bind(this, idx)}
            onBitCountChange={this.reloadList} />
        )
      }
      </tbody>
    </table>;
  }
}





export interface BitRowProperties {
  bit: OutputBit
  availableVariations: VariationBundle[]
  onBitModified?: (bit: OutputBit) => void
  onBitCountChange?: (newBits: number) => void
}

export interface BitRowState {
  bit: OutputBit
}

export class BitRow extends React.Component<BitRowProperties, BitRowState> {
  constructor(props: BitRowProperties) {
    super(props);

    this.state = {
      bit: props.bit
    }
  }

  findIndex(bit: OutputBit = this.props.bit): number {
    return getData().bits.findIndex(b => b.timestamp == bit.timestamp);
  }

  override render(): JSX.Element {
    const { bit } = this.state;

    return <tr className='bit'>
      <td className='bit-instant'>
        <Input
          type='checkbox'
          data={bit}
          dataKey='instant'
          onChange={this.updateBitState} />
      </td>
      <td className='bit-gym'>
        <Select
          type='number'
          data={bit}
          dataKey='gymId'
          onChange={this.updateBitState} >
          {
            getData().gyms.map((name, idx) => 
              <option key={idx} value={idx+1}>{name}</option>
            )
          }
        </Select>
      </td>
      <td className='bit-super-set'>
        <Input
          type='number'
          data={bit}
          dataKey='superSet'
          onChange={this.updateBitState} />
      </td>
      <td className='bit-reps'>
        <Input
          type='number'
          data={bit}
          dataKey='reps'
          onChange={this.updateBitState} />
      </td>
      <td className='bit-weight'>
        <Input
          type='number'
          data={bit}
          dataKey='totalWeight'
          onChange={this.updateBitState} />
      </td>
      <td className='bit-note'>
        <Input
          type='text'
          data={bit}
          dataKey='note'
          onChange={this.updateBitState} />
      </td>
      <td className='bit-variation-select'>
        <Select
          type='number'
          data={bit}
          dataKey='variationId'
          onChange={this.updateBitState} >
          {
            this.props.availableVariations.map(({variation, exercise}, idx) =>
              <option key={idx} value={variation.variationId}>
                {variation.variationId}: { variation.def ? exercise.name : `${exercise.name} - ${variation.name}` }
              </option>
            )
          }
        </Select>
      </td>
      <td className='bit-timestamp'>
        <Input
          type='datetime-local'
          data={bit}
          dataKey='timestamp'
          linkValue={true}
          onChange={this.updateBitState} />
      </td>
      <td className='bit-actions'>
        <button onClick={this.moveBit.bind(this, true)}>↑</button>
        <button onClick={this.moveBit.bind(this, false)}>↓</button>
        <button onClick={this.newBit.bind(this, true)} className='green'>+↑</button>
        <button onClick={this.newBit.bind(this, false)} className='green'>+↓</button>
        <button onClick={this.remove} className='transparent'>×</button>
      </td>
    </tr>;
  }

  save = (bit: OutputBit, originalBit: OutputBit) => {
    setData(data => {
      const index = data.bits.findIndex(b => b.timestamp == originalBit.timestamp);
      data.bits[index] = bit;
    });
    if (this.props.onBitModified) {
      this.props.onBitModified(bit);
    }
  }

  remove = () => {
    setData(data => {
      const { bit } = this.props;
      const index = data.bits.findIndex(b => b.timestamp == bit.timestamp);
      data.bits.splice(index, 1);
    });
    if (this.props.onBitCountChange) {
      this.props.onBitCountChange(-1);
    }
  }

  generateNewTimestamp(current: number, top = false) {
    const date = new Date(current);
    date.setSeconds(top? date.getSeconds() - 1 : date.getSeconds() + 1);

    let timestamp = date.getTime();
    while (getData().bits.findIndex(b => b.timestamp == timestamp) >= 0) {
      date.setMilliseconds(top? date.getMilliseconds() + 1 : date.getMilliseconds() - 1);
      timestamp = date.getTime();
    }

    return timestamp;
  }

  newBit(top = false) {
    setData(data => {
      const { bit } = this.props;
      const index = data.bits.findIndex(b => b.timestamp == bit.timestamp);

      const stateBit = this.state.bit;
      const newBit = {
        ...stateBit,
        timestamp: this.generateNewTimestamp(stateBit.timestamp, top)
      };

      data.bits.splice(top? index : index + 1, 0, newBit);
    });
    if (this.props.onBitCountChange) {
      this.props.onBitCountChange(1);
    }
  }

  moveBit(top = false) {
    setData(data => {
      const { bit } = this.props;
      const index = data.bits.findIndex(b => b.timestamp == bit.timestamp);

      const newBit = { ...this.state.bit };

      data.bits.splice(index, 1);
      data.bits.splice(top? index - 1 : index + 1, 0, newBit);
    });
    if (this.props.onBitCountChange) {
      this.props.onBitCountChange(0);
    }
  }

  updateBitState = (bitState: OutputBit, originalBit: OutputBit) => {
    if (bitState.note && !bitState.note.trim().length) {
      delete bitState.note;
    }
    this.setState({ bit: bitState });
    this.save(bitState, originalBit);
  }
}
