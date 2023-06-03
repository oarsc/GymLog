import React, { ChangeEvent, PropsWithChildren } from 'react';

export interface ExtendedSelectProperties<T> extends PropsWithChildren, Omit<React.HTMLProps<HTMLSelectElement>, 'data' | 'onChange' | 'defaultValue'> {
  type?: 'text' | 'number'
  data: T
  dataKey: keyof T
  onChange?: (value: T, original: T) => void
  linkValue?: boolean
}

interface DataValueType {
  'text': string;
  'number': number;
}

export default class Select<T> extends React.Component<ExtendedSelectProperties<T>> {

  constructor(props: ExtendedSelectProperties<T>) {
    super(props);
  }

  getLinkedValue<K extends keyof DataValueType>(type: K) : DataValueType[K] | undefined {
    return this.props.data[this.props.dataKey] as DataValueType[K] | undefined;
  }

  handleOnChange = (ev: ChangeEvent<HTMLSelectElement>) => {
    const { data, dataKey, onChange } = this.props;

    const value = this.mapInputValue(ev.target);

    const newData: T = {
      ...data,
      [dataKey]: value
    }

    if (value === undefined) {
      delete newData[dataKey];
    }

    if (onChange) onChange(newData, data);
  }

  mapInputValue(input: HTMLSelectElement): string | number | undefined {
    if (!input.value.length) 
      return undefined;

    switch(this.props.type) {
      case 'number':         return parseInt(input.value);
      default:               return input.value;
    }
  }

  setValueToHtmlProps(htmlProps: React.HTMLProps<HTMLSelectElement>) {
    if (htmlProps.value !== undefined) {
      if (!this.props.linkValue) {
        htmlProps.defaultValue = htmlProps.value;
        delete htmlProps.value;
      }
      return;
    }

    const value = this.getLinkedValue(this.props.type ?? 'text');

    if (this.props.linkValue)
      htmlProps.value = value ?? '';
    else
      htmlProps.defaultValue = value ?? '';
  }

  override render() {
    const htmlProps =  {
      ...this.props,
      type: undefined,
      data: undefined,
      dataKey: undefined,
      onChange: undefined,
    };
    
    this.setValueToHtmlProps(htmlProps)
    
    delete htmlProps.data;
    delete htmlProps.dataKey;
    delete htmlProps.linkValue;

    return <select {...htmlProps} onChange={this.handleOnChange}>
        { this.props.children }
      </select>;
  }
}
