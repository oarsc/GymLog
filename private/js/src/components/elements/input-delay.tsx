import React, { ChangeEvent } from 'react';


interface InputDelayProperties extends React.HTMLProps<HTMLInputElement> {
  time?: number,
  onStart?: () => void | undefined,
  onResolve?: () => void | undefined,
}

export default class DelayInput extends React.Component<InputDelayProperties> {

  delayedOnChange(props: InputDelayProperties) {
    const { time, onChange, onStart, onResolve } = props;
    if (!onChange) return;

    let timeout: NodeJS.Timeout | undefined;
    return function(ev: ChangeEvent<HTMLInputElement>) {
      if (timeout)      clearTimeout(timeout);
      else if (onStart) onStart();

      timeout = setTimeout(() => {
        onChange(ev);
        timeout = undefined;
        if (onResolve) onResolve();
      }, time ?? 300);
    }
  }

  override render() {
      return <input {...this.props} onChange={this.delayedOnChange(this.props)}/>;
  }
}
