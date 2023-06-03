import './exercise.scss';
import React, { ChangeEvent, MouseEvent } from 'react';
import { Output, OutputExercise, OutputMuscleRelation, OutputVariation } from '../../../model/output-format';
import Accordion from '../elements/accordion';
import { AddMessageFunction } from '../elements/messages';
import { sort } from '../../utils';
import Input from '../elements/custom-input';
import ExerciseImageSelect from '../elements/exercise-img-select';
import ExerciseMuscleSelect from '../elements/exercise-muscle-select';
import { Muscle } from '../../../model/muscle';
import MuscleSelect from '../elements/muscle-select';
import Select from '../elements/custom-select';
import { dataObservable, getData, getDataSortedExercises, setData } from '../../data-manager';

interface ExerciseProperties {
  addMessage: AddMessageFunction
  onGlobalExercise?: (exerciseId: number) => void
}

interface ExerciseState {
  idFilter: number
  exerciseFilter: string
  muscleFilter?: Muscle
  exercises: OutputExercise[]
}

export default class ExerciseBlock extends React.Component<ExerciseProperties, ExerciseState> {
  constructor(props: ExerciseProperties) {
    super(props);

    dataObservable().subscribe(_ => {
      this.setState({
        exercises: getDataSortedExercises()
      })
    });

    this.state = {
      idFilter: 0,
      exerciseFilter: '',
      exercises: getDataSortedExercises()
    }
  }

  updateState = (state: ExerciseState) => this.setState(state);
  updateMuscleFilter = (muscle: Muscle | undefined) => this.setState({ muscleFilter: muscle });
  refreshExerciseList = () => this.setState({ exercises: getDataSortedExercises(true) })

  override render(): JSX.Element {
    return (
      <Accordion title='Exercises' collapse={false}>
        <div id='exercise-filter'>
          <Input
            type='text'
            data={this.state}
            dataKey='exerciseFilter'
            time={300}
            placeholder='Exercise filter'
            onChange={this.updateState} 
            autoFocus />

          <MuscleSelect
            noneText='--'
            onChange={this.updateMuscleFilter} />

          <Input
            type='number'
            placeholder='Id filter'
            data={this.state}
            dataKey='idFilter'
            onChange={this.updateState} />
        </div>
        <div id='exercises'>
          {
            this.state.exercises
              .map(exercise =>
                <ExerciseRow
                  key={exercise.exerciseId}
                  exercise={exercise}
                  hide={this.isFilteredOut(exercise)}
                  addMessage={this.props.addMessage}
                  onGlobalExercise={this.props.onGlobalExercise}
                  onExerciseListModified={this.refreshExerciseList} />
              )
          }
        </div>
      </Accordion>
    );
  }

  isFilteredOut(exercise: OutputExercise): boolean {
    const { idFilter, muscleFilter, exerciseFilter } = this.state;

    const _variations = () => getData().variations.filter(va => va.exerciseId === exercise.exerciseId);

    if (idFilter > 0) {
      return exercise.exerciseId === idFilter
        ? false
        : !_variations().some(va => va.variationId === idFilter);
    }

    if (!exerciseFilter && !muscleFilter) {
      return true;
    }

    if (muscleFilter) {
      const found = getData().primaries.find(rel => 
        rel.muscleId === muscleFilter.id &&
        rel.exerciseId === exercise.exerciseId
      );

      if (!found) return true;
    }

    const filterValue = exerciseFilter.toLowerCase();
    if (exercise.name.toLowerCase().indexOf(filterValue) >= 0) {
      return false;
    }
    return !_variations().some(va => va.name.toLowerCase().indexOf(filterValue) >= 0);
  }
}


interface ExerciseRowProperties {
  exercise: OutputExercise
  hide: boolean
  addMessage: AddMessageFunction
  onGlobalExercise?: (exerciseId: number) => void;
  onExerciseListModified?: () => void;
}

interface ExerciseRowState {
  exercise: OutputExercise
  variations: OutputVariation[]
  collapsed: boolean
}

class ExerciseRow extends React.Component<ExerciseRowProperties, ExerciseRowState> {

  constructor(props: ExerciseRowProperties) {
    super(props);

    dataObservable().subscribe(data => 
      this.setState({
        variations: this.getVariations(data)
      })
    );

    this.state = {
      exercise: { ... props.exercise },
      variations: this.getVariations(),
      collapsed: true
    }
  }

  getVariations(data: Output = getData()) {
    return data.variations.filter(va => va.exerciseId === this.props.exercise.exerciseId);
  }

  toggle() {
    this.setState({
      collapsed: !this.state.collapsed
    })
  }

  onGlobalExercise = (ev: MouseEvent<HTMLButtonElement>) => {
    if (this.props.onGlobalExercise) {
      this.props.onGlobalExercise(this.state.exercise.exerciseId);
    }
  }

  refreshVariationList = () => this.setState({ variations: this.getVariations() })

  override render(): JSX.Element {
    if (this.props.hide) {
      return <></>;
    }
    const exercise = this.state.exercise;

    return (
      <div className={`exercise ${this.state.collapsed? 'collapsed': ''}`}>
        <div className='header' onClick={this.toggle.bind(this)}>
          <img src={ `/img/previews/${exercise.image}.png` }/>
          <span>{exercise.name}</span>
        </div>
        <Accordion collapse={this.state.collapsed}>
          <div className='exercise-details'>
            <table>
              <tbody>
                <tr>
                  <td className='title'>Exercise #{ exercise.exerciseId }</td>
                  <td colSpan={2}>
                    <Input
                      className='name'
                      type='text'
                      data={exercise}
                      dataKey='name'
                      onChange={this.updateExerciseState} />
                  </td>
                  <td style={{textAlign: 'center'}}>
                    <ExerciseImageSelect
                      value={exercise.image}
                      onChange={this.updateExerciseImageState} />
                  </td>
                </tr>
                <tr>
                  <td colSpan={2}>Primary</td>
                  <td colSpan={2}>Secondary</td>
                </tr>
                <tr>
                  <td colSpan={2} className='muscles'>
                    <ExerciseMuscleSelect 
                      relations={getData().primaries}
                      exercise={exercise}
                      onChange={this.updatePrimaries} />
                  </td>
                  <td colSpan={2} className='muscles'>
                    <ExerciseMuscleSelect 
                      relations={getData().secondaries}
                      exercise={exercise}
                      onChange={this.updateSecondaries} />
                  </td>
                </tr>
              </tbody>
            </table>

            {
              this.state.variations.map(variation => 
                <VariationRow
                key={variation.variationId}
                variation={variation}
                exercise={exercise}
                hide={false}
                addMessage={this.props.addMessage}
                onVariationListModified={this.refreshVariationList} />
              )
            }

            <div className='actions'>
              <button onClick={this.remove} className='transparent'>×</button>
              <button onClick={this.copyExercise} className='green'>+↓</button>
              {
                this.props.onGlobalExercise &&
                <button onClick={this.onGlobalExercise}>↓↓</button>
              }
            </div>
          </div>
        </Accordion>
      </div>
    );
  }

  copyExercise = () => {
    setData(data => {
      const exercise = this.state.exercise;

      const newId = 1 + data.exercises
        .map(v => v.exerciseId)
        .reduce((max, id) => id > max ? id : max, 0);
  
      const newExercise = {
        ...exercise,
        exerciseId: newId
      }
  
      const primaries: OutputMuscleRelation[] = data.primaries
        .filter(rel => rel.exerciseId === exercise.exerciseId)
        .map(rel => ({ muscleId: rel.muscleId, exerciseId: newId }));
  
      const secondaries: OutputMuscleRelation[] = data.secondaries
        .filter(rel => rel.exerciseId === exercise.exerciseId)
        .map(rel => ({ muscleId: rel.muscleId, exerciseId: newId }));
  
      const newVariationId = 1 + data.variations
        .map(v => v.variationId)
        .reduce((max, id) => id > max ? id : max, 0);
  
      const newVariation = {
        ...data.variations
          .filter(va => va.def)
          .filter(va => va.exerciseId === exercise.exerciseId)[0],
        variationId: newVariationId,
        exerciseId: newId
      }

      return {
        ...data,
        exercises: [
          ...data.exercises,
          newExercise
        ],
        variations: [
          ...data.variations,
          newVariation
        ],
        primaries: [
          ...data.primaries,
          ...primaries
        ],
        secondaries: [
          ...data.secondaries,
          ...secondaries
        ]
      }
    });
    if (this.props.onExerciseListModified)
      this.props.onExerciseListModified();
  }

  remove = () => {
    setData(data => {
      const exercise = this.state.exercise;

      const exercises = data.exercises
        .filter(ex => ex.exerciseId !== exercise.exerciseId)

      const primaries: OutputMuscleRelation[] = data.primaries
        .filter(rel => rel.exerciseId !== exercise.exerciseId)

      const secondaries: OutputMuscleRelation[] = data.secondaries
        .filter(rel => rel.exerciseId !== exercise.exerciseId);

      const variations: OutputVariation[] = data.variations
        .filter(va => va.exerciseId !== exercise.exerciseId);
      
      return {
        ...data,
        exercises,
        primaries,
        secondaries,
        variations
      };
    });
    if (this.props.onExerciseListModified)
      this.props.onExerciseListModified();
  }

  save = (exercise: OutputExercise) => {
    setData(data => ({
      ...data,
      exercises: data.exercises.map(ex => ex.exerciseId === exercise.exerciseId ? exercise : ex),
    }));
  }

  updateExerciseState = (exerciseState: OutputExercise) => {
    this.setState({ exercise: exerciseState });
    this.save(exerciseState);
  }

  updateExerciseImageState = (image: string) => {
    const exerciseState = {
      ...this.state.exercise,
      image
    };
    this.updateExerciseState(exerciseState);
  }

  updatePrimaries = (muscles: Muscle[]) => {
    setData(data => ({
      ...data,
      primaries: this.generateNewRelations(data.primaries, muscles)
    }));
  }

  updateSecondaries = (muscles: Muscle[]) => {
    setData(data => ({
      ...data,
      secondaries: this.generateNewRelations(data.secondaries, muscles)
    }));
  }

  generateNewRelations = (relations: OutputMuscleRelation[], muscles: Muscle[]) => {
    const newRelations = relations.filter(rel => rel.exerciseId !== this.state.exercise.exerciseId);

    muscles.map(m => ({
      exerciseId: this.state.exercise.exerciseId,
      muscleId: m.id,
    })).forEach(rel => newRelations.push(rel));

    return newRelations
      .sort(sort(r => r.muscleId))
      .sort(sort(r => r.exerciseId));
  }
}

interface VariationRowProperties {
  variation: OutputVariation
  exercise: OutputExercise
  hide: boolean
  addMessage: AddMessageFunction
  onVariationListModified?: () => void
}

interface VariationRowState {
  variation: OutputVariation
  collapsed: boolean
}

class VariationRow extends React.Component<VariationRowProperties, VariationRowState> {

  constructor(props: VariationRowProperties) {
    super(props);
    this.state = {
      variation: { ... this.props.variation },
      collapsed: true
    }
  }

  toggle() {
    this.setState({
      collapsed: !this.state.collapsed
    })
  }

  override render(): JSX.Element {
    if (this.props.hide) {
      return <></>;
    }
    const exercise = this.props.exercise;
    const variation = this.state.variation;

    return (
      <div className={`variation ${this.state.collapsed? 'collapsed': ''}`}>
        <div className='header' onClick={this.toggle.bind(this)}>
          <img src={ `/img/previews/${exercise.image}.png` }/>
          <span>{exercise.name}</span> <span className='variation-name'>{variation.name}</span>
        </div>
        <Accordion collapse={this.state.collapsed}>
          <div className='variation-details'>
            <table>
              <tbody>
                <tr>
                  <td>Variation</td>
                  <td>{ exercise.exerciseId } - { variation.variationId }</td>
                  <td><label>
                    <Input
                      type='checkbox'
                      data={variation}
                      dataKey='def'
                      onChange={this.updateVariationDefault} />
                    default
                  </label></td>
                  <td>
                    <Input
                      type='text'
                      disabled={variation.def}
                      data={variation}
                      linkValue={true}
                      dataKey='name'
                      onChange={this.updateVariationState} />
                  </td>
                </tr>
                <tr>
                  <td>Gym relation</td>
                  <td>
                    <Select
                      type='number'
                      data={variation}
                      dataKey='gymRelation'
                      onChange={this.updateGymRelation} >
                      <option value='0'>NO_RELATION</option>
                      <option value='1'>INDIVIDUAL_RELATION</option>
                      <option value='2'>STRICT_RELATION</option>
                    </Select>
                  </td>

                  <td>Gym Id</td>
                  <td>
                    <Select
                      type='number'
                      data={variation}
                      dataKey='gymId'
                      linkValue={true}
                      disabled={variation.gymRelation < 2}
                      onChange={this.updateVariationState} >
                      <option value=''>NONE</option>
                      {
                        getData().gyms.map((name, idx) => 
                              <option key={idx} value={idx+1}>{name}</option>
                        )
                      }
                    </Select>
                  </td>
                </tr>
                <tr>
                  <td>Exercise Type</td>
                  <td>
                    <Select
                      type='number'
                      data={variation}
                      dataKey='type'
                      onChange={this.updateVariationState} >
                      <option value='0'>NONE</option>
                      <option value='1'>DUMBBELL</option>
                      <option value='2'>BARBELL</option>
                      <option value='3'>PLATE</option>
                      <option value='4'>PULLEY_MACHINE</option>
                      <option value='5'>SMITH_MACHINE</option>
                      <option value='6'>MACHINE</option>
                      <option value='7'>CARDIO</option>
                    </Select>
                  </td>
                </tr>
                <tr>
                  <td>Last bar</td>
                  <td>
                    <Select
                      type='number'
                      data={variation}
                      dataKey='lastBarId'
                      onChange={this.updateVariationState} >
                        <option value=''>NONE</option>
                        <option value='1'>7.5 kg</option>
                        <option value='2'>10 kg</option>
                        <option value='3'>12 kg</option>
                        <option value='4'>15 kg</option>
                        <option value='5'>20 kg</option>
                        <option value='6'>25 kg</option>
                    </Select>
                  </td>

                  <td>Last Rest Time</td>
                  <td>
                    <Input
                      type='number'
                      value={variation.lastRestTime < 0? '' : variation.lastRestTime}
                      linkValue={true}
                      disabled={variation.lastRestTime < 0}
                      data={variation}
                      dataKey='lastRestTime'
                      onChange={this.updateVariationState} />
                    <input type='checkbox' checked={variation.lastRestTime < 0} onChange={this.updateDefaultRestTime}/>
                  </td>
                </tr>
                <tr>
                <td>Last Step</td>
                  <td>
                    <Input
                      type='number'
                      data={variation}
                      dataKey='lastStep'
                      onChange={this.updateVariationState} />
                  </td>

                  <td>Last Weight Spec</td>
                  <td>
                    <Select
                      type='number'
                      data={variation}
                      dataKey='lastWeightSpec'
                      onChange={this.updateVariationState} >
                        <option value='0'>TOTAL_WEIGHT</option>
                        <option value='1'>NO_BAR_WEIGHT</option>
                        <option value='2'>ONE_SIDE_WEIGHT</option>
                    </Select>
                  </td>
                </tr>
              </tbody>
            </table>
            <div className='actions'>
              <button onClick={this.remove} className='transparent'>×</button>
              <button onClick={this.copyVariation} className='green'>+↓</button>
            </div>
          </div>
        </Accordion>
      </div>
    );
  }

  copyVariation = () => {
    setData(data => {
      const newId = 1 + data.variations
        .map(v => v.variationId)
        .reduce((max, id) => id > max ? id : max, 0);
  
      const newVariation = {
        ...this.state.variation,
        variationId: newId
      };

      return {
        ...data,
        variations: [
          ...data.variations,
          newVariation
        ]
      };
    });
    if (this.props.onVariationListModified)
      this.props.onVariationListModified();
  }

  remove = () => {
    setData(data => {
      const variation = this.state.variation;

      const exesVariables = data.variations
        .filter(va => va.exerciseId === variation.exerciseId);
  
      if (exesVariables.length < 2) {
        this.props.addMessage('Can\'t leave exercise without variations');
        return;
      }
  
      const variations: OutputVariation[] = data.variations
        .filter(va => va.variationId !== variation.variationId);
      
      return {
        ...data,
        variations
      };
    });
    if (this.props.onVariationListModified)
      this.props.onVariationListModified();
  }

  save = (variation: OutputVariation) => {
    setData(data => ({
      ...data,
      variations: data.variations.map(va => va.variationId === variation.variationId ? variation : va),
    }));
  }

  updateVariationState = (variationState: OutputVariation) => {
    this.setState({ variation: variationState });
    this.save(variationState);
  }

  updateGymRelation = (variationState: OutputVariation) => {
    if (variationState.gymRelation < 2)
      delete variationState.gymId;

    this.updateVariationState(variationState);
  }

  updateVariationDefault = (variationState: OutputVariation) => {
    if (variationState.def)
      variationState.name = '';

    this.updateVariationState(variationState);
  }

  updateDefaultRestTime = (ev: ChangeEvent<HTMLInputElement>) => {
    const variationState = {
      ...this.state.variation,
      lastRestTime: ev.target.checked? -1 : 90
    };

    this.updateVariationState(variationState);
  }
}
