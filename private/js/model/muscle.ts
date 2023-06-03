export interface Muscle {
  id: number,
  name: string,
  color: string,
}

export const MUSCLES: Muscle[] = [
  { id: 1,  name: 'Chest (Pectoral)',    color: '#9675CE' },
  { id: 2,  name: 'Upper Back',          color: '#1073AE' },
  { id: 3,  name: 'Lower Back',          color: '#4DD0E2' },
  { id: 4,  name: 'Shoulders (Deltoid)', color: '#E05E55' },
  { id: 5,  name: 'Trapezius',           color: '#F05F92' },
  { id: 6,  name: 'Biceps',              color: '#8DCA1A' },
  { id: 7,  name: 'Triceps',             color: '#01BFA5' },
  { id: 8,  name: 'Forearm',             color: '#71BA6E' },
  { id: 9,  name: 'Quadriceps',          color: '#FFD33F' },
  { id: 10, name: 'Hamstrings',          color: '#F9A825' },
  { id: 11, name: 'Glutes',              color: '#F96825' },
  { id: 12, name: 'Calves',              color: '#B7AB6E' },
  { id: 13, name: 'Abdominals',          color: '#CA62F0' },
  { id: 14, name: 'Cardio',              color: '#AAABAA' },
];