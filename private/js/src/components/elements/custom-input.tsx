import React, { ChangeEvent } from 'react';

interface InputProperties<T> extends Omit<React.HTMLProps<HTMLInputElement>, 'data' | 'onChange' | 'defaultValue'> {
  type: 'text' | 'checkbox' | 'number' | 'date' | 'time' | 'datetime-local'
  data: T
  dataKey: keyof T
  useDate?: boolean
  onChange?: (value: T, original: T) => void
  time?: number
  linkValue?: boolean
}

interface DataValueType {
  'text': string;
  'checkbox': boolean;
  'number': number;
  'date': number | Date;
  'time': number | Date;
  'datetime-local': number | Date;
}

export default class Input<T> extends React.Component<InputProperties<T>> {
  inputRef: React.RefObject<HTMLInputElement> = React.createRef();

  getDataValue<K extends keyof DataValueType>(type: K) : DataValueType[K] | undefined {
    return this.props.data[this.props.dataKey] as DataValueType[K] | undefined;
  }

  castValue<K extends keyof DataValueType>(_: K, value: string | number | boolean | Date) : DataValueType[K] {
    return value as DataValueType[K];
  }

  fixInputValue() {
    if (this.props.linkValue) {
      return;
    }

    const input = this.inputRef.current;
    if (input) {
      if (this.props.type === 'number') {
        input.value = input.value.replace(/[^0-9]/g, '');
      }
    }
  }

  delayedOnChange(props: InputProperties<T>) {
    this.fixInputValue();

    const { time, data, dataKey, onChange } = props;

    const handleOnChange = (ev: ChangeEvent<HTMLInputElement>) => {
      const newData: T = {
        ...data,
        [dataKey]: this.mapInputValue(ev.target) ?? data[dataKey]
      }
  
      if (onChange) onChange(newData, data);
    }

    if (!time) {
      return handleOnChange;
    }

    let timeout: NodeJS.Timeout | undefined;
    return function(ev: ChangeEvent<HTMLInputElement>) {
      clearTimeout(timeout);
      timeout = setTimeout(() => handleOnChange(ev), time);
    }
  }

  mapInputValue(input: HTMLInputElement): string | number | boolean | Date | undefined {
    switch(this.props.type) {
      case 'number':         return parseInt(input.value.replace(/[^0-9]/g, '') || '0');
      case 'checkbox':       return input.checked;
      case 'date':           return !input.valueAsDate? undefined : this.props.useDate? input.valueAsDate : input.valueAsNumber;
      case 'time':           return !input.valueAsDate? undefined : this.props.useDate? input.valueAsDate : input.valueAsNumber;
      case 'datetime-local': return !input.valueAsDate? undefined : this.props.useDate? new Date(input.value) : new Date(input.value).getTime();
      default:               return input.value;
    }
  }

  setValueToHtmlProps(htmlProps: React.HTMLProps<HTMLInputElement>) {
    if (htmlProps.value !== undefined) {
      if (!this.props.linkValue) {
        htmlProps.defaultValue = htmlProps.value;
        delete htmlProps.value;
      }
      return;
    }

    const type = this.props.type;
    const value = this.getDataValue(type);
    if (value === undefined)
      return;
    
    
    const setValue = (val: string | number | readonly string[] | undefined) => {
      if (this.props.linkValue)
        htmlProps.value = val;
      else
        htmlProps.defaultValue = val;
    }

    switch(type) {
      case 'date': {
        const safeValue = this.castValue(type, value);
        const ten = (i: number) => (i < 10 ? '0' : '') + i;
        const date = typeof safeValue == 'number'? new Date(safeValue) : safeValue;
        const YYYY = date.getFullYear(),
          MM = ten(date.getMonth() + 1),
          DD = ten(date.getDate());
        
        setValue(`${YYYY}-${MM}-${DD}`);
        break;
      }
      
      case 'time': {  
        const safeValue = this.castValue(type, value);
        const ten = (i: number) => (i < 10 ? '0' : '') + i;
        const date = typeof safeValue == 'number'? new Date(safeValue) : safeValue;
        const HH = ten(date.getHours()),
          II = ten(date.getMinutes()),
          SS = ten(date.getSeconds());
        
        setValue(`${HH}:${II}:${SS}`);
        break;
      }
      
      case 'datetime-local': {
        const safeValue = this.castValue(type, value);
        const ten = (i: number) => (i < 10 ? '0' : '') + i;
        const hundred = (i: number) => (i < 10 ? '00' : i < 100 ? '0' : '') + i;
        const date = typeof safeValue == 'number'? new Date(safeValue) : safeValue;
        const YYYY = date.getFullYear(),
          MM = ten(date.getMonth() + 1),
          DD = ten(date.getDate()),
          HH = ten(date.getHours()),
          II = ten(date.getMinutes()),
          SS = ten(date.getSeconds()),
          MMM = hundred(date.getMilliseconds());

        setValue(`${YYYY}-${MM}-${DD} ${HH}:${II}:${SS}.${MMM}`);
        break;
      }

      case 'checkbox':
        if (typeof this.props.linkValue === 'boolean' && this.props.linkValue)
          htmlProps.checked ??= this.castValue(type, value);
        else
          htmlProps.defaultChecked ??= this.castValue(type, value);
        break;

      case 'number':
        setValue(this.castValue(type, value || ''));
        break;

      default:
        setValue(this.castValue(type, value));
        break;
    }
  }

  override render() {
    const htmlProps =  {
      ...this.props,
      type: this.props.type == 'number'? 'text' : this.props.type,
      data: undefined,
      dataKey: undefined,
      onChange: undefined,
    };
    
    this.setValueToHtmlProps(htmlProps)
    
    delete htmlProps.data;
    delete htmlProps.dataKey;
    delete htmlProps.useDate;
    delete htmlProps.time;
    delete htmlProps.linkValue;

    return <input {...htmlProps} onChange={this.delayedOnChange(this.props)} ref={this.inputRef}/>;
  }
}
