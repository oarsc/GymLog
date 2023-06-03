export function sort<T,R>(callback: (input: T) => R) {
  return (a:T, b:T) => {
    const va = callback(a);
    const vb = callback(b);
    return va > vb ? 1 : va < vb ? -1 : 0
  }
}
