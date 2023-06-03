import React from 'react';
import { ajax, createElement } from '../../_dom/dom-utils';
import { AddMessageFunction } from './elements/messages';
import { forceDataReload, getData, setData } from '../data-manager';
import { sort } from '../utils';
import { Output } from '../../model/output-format';

interface MainActionsProperties {
  addMessage: AddMessageFunction
}

interface MainActionsState {
  exerciseFilter: string
};

export default class MainAction extends React.Component<MainActionsProperties, MainActionsState> {

  constructor(props: MainActionsProperties) {
    super(props);

    this.state = {
      exerciseFilter: ''
    }

    window.addEventListener("keydown", ev => {
      if (ev.key == 'F5' && !ev.ctrlKey && !ev.shiftKey && !ev.altKey) {
        forceDataReload();
        ev.preventDefault();
        ev.stopPropagation();
      }
    });
  }

  download = () => {
    if (!this.validate()) {
      return;
    }

    const blob = new Blob([
      JSON.stringify(setData(d => this.optimizeData(d)), null, 1)
    ], {type: 'application/json'});

    const elem = createElement('a', undefined, document.body);
    elem.href = window.URL.createObjectURL(blob);
    elem.download = 'filename.json';        
    elem.click();   
    elem.remove();
  }

  save = async () => {
    if (!this.validate()) {
      return;
    }

    await this.saveFile(setData(d => this.optimizeData(d)));
  }

  async saveFile(data: Output) {
    await ajax('/save', data, { method: 'POST' })
      .then(res => res.text())
      .then(content => this.props.addMessage(content));
  }

  syncVars = async () => {
    if (!this.validate()) {
      return;
    }

    setData(data => {
      this.optimizeData(data);

      const variationsMap = data.variations
        .filter(va => va.def)
        .red((obj, va) => obj[va.variationId] = va.exerciseId, {} as { [key: number]: number});

      let nextId = Object.values(variationsMap).reduce((max, cur) => max > cur ? max : cur, 0);

      data.variations
        .filter(va => !va.def)
        .map(va => va.variationId)
        .forEach(id => variationsMap[id] = ++nextId);

      data.variations.forEach(va => va.variationId = variationsMap[va.variationId]);
      data.bits.forEach(bit => bit.variationId = variationsMap[bit.variationId]);
    });

    forceDataReload();
    this.props.addMessage('Variations IDs updated');
  }

  validate(): boolean {
    const { variations, exercises, bits } = getData();

    const result = exercises.map(ex => {
      const vars = variations.filter(va => va.exerciseId === ex.exerciseId);
      if (!vars.length)    return `Exercise ${ex.exerciseId} has no variations`;

      const defs = vars.filter(va => va.def);
      if (!defs.length)    return `Exercise ${ex.exerciseId} has no default variations`;
      if (defs.length > 1) return `Exercise ${ex.exerciseId} has more than one default variation`;

      return '';
    }).filter(Boolean);

    const bitsNoVars = bits
      .map(b => b.variationId)
      .filter((val, idx, arr) => arr.indexOf(val) === idx)
      .filter(varId => !variations.find(v => v.variationId === varId))
      .join(', ');

    if (bitsNoVars.length) {
      result.push(`Some bits have these unexistant variations: ${bitsNoVars}`);
    }

    const duplicatedTimestamp = bits
      .map(bit => bit.timestamp)
      .find((val, idx, list) => list.indexOf(val) !== idx);

    if (duplicatedTimestamp) {
      result.push(`More than one bit share timestamp: ${duplicatedTimestamp}`);
    }

    if (result.length) {
      this.props.addMessage(result.join('\n'));
      return false;
    }
    return true;
  }

  optimizeData(data: Output) {
    data.bits
      .sort(sort(b => b.timestamp));
    data.exercises
      .sort(sort(e => e.exerciseId));
    data.variations
      //.sort(sort(v => v.variationId))
      .sort(sort(v => v.exerciseId));
    data.trainings
      .sort(sort(t => t.trainingId));
    data.primaries
      .sort(sort(r => r.exerciseId))
      .sort(sort(r => r.muscleId));
    data.secondaries
      .sort(sort(r => r.exerciseId))
      .sort(sort(r => r.muscleId));

    data.prefs = this.sortObjectKeys(data.prefs);
    data.exercises = data.exercises.map(this.sortObjectKeys);
    data.variations = data.variations.map(this.sortObjectKeys);
    data.trainings = data.trainings.map(this.sortObjectKeys);
    data.primaries = data.primaries.map(this.sortObjectKeys);
    data.secondaries = data.secondaries.map(this.sortObjectKeys);
    data.bits = data.bits.map(bit => {
      const nBit = this.sortObjectKeys(bit);
      if (nBit.note !== undefined) {
        const note = nBit.note.trim();
        if (note) nBit.note = note
        else      delete nBit.note;
      }
      if (nBit.instant === false) {
        delete nBit.instant;
      }
      if (nBit.superSet === 0) {
        delete nBit.superSet;
      }
      return nBit;
    });

    data.trainings.forEach(tr => {

      const [ minTimestamp, maxTimestamp ] = data.bits
        .filter(bit => bit.trainingId == tr.trainingId)
        .map(bit => bit.timestamp)
        .reduce((limits, timestamp) =>
          [
            limits[0] < timestamp ? limits[0] : timestamp,
            limits[1] > timestamp ? limits[1] : timestamp
          ]
        , [new Date().getTime(), 0]);

      if (minTimestamp < maxTimestamp) {
        tr.start = minTimestamp
        tr.end = maxTimestamp
      }
    });
  }

  sortObjectKeys<T extends Object>(object: T): T {
    const objectKeys = Object.keys(object) as Array<keyof T>;

    return objectKeys
      .sort()
      .red((obj: T, key: keyof T) => obj[key] = object[key], {} as T);
  }

  override render(): JSX.Element {
    return <>
      {/*<button onClick={this.download}>Download</button>*/}
      <button onClick={this.save}>Save</button>
      <button onClick={this.syncVars}>Update variations ids</button>
      <button onClick={forceDataReload}>Refresh</button>
    </>;
  }
}
