import { Router } from 'express';
import * as fs from 'fs';
import path from 'path';
import { handleError } from '../error';

export const router = Router();

/* GET users listing. */
router.get('/', (req, res, next) => {
  fs.readFile(path.join(__dirname, '../../bak/output.json'), 'utf8', (err, content) => {
    handleError(err, 404, next, () => {
      res.json(outputDecode(JSON.parse(content)));
    });
  });
});

router.post('/', (req, res, next) => {
  fs.writeFile('bak/output.json', JSON.stringify(outputEncode(req.body), null, 1), err => {});
  res.send('Saved')
});

router.get('/images', (req, res, next) => {
  fs.readdir('../GymLog/app/src/main/assets/previews/', (err, files) => {
    res.json(files.map(f => f.replace(/\.\w{3}$/, '')));
  })
});


function outputDecode(json: any) {
  json.bits.forEach((bit: any) => {
    bit.timestamp = bit.s;
    bit.totalWeight = bit.w;
    bit.trainingId = bit.t;
    bit.variationId = bit.v;
    bit.note = json.notes[bit.n]?.trim();

    delete bit.s;
    delete bit.t;
    delete bit.v;
    delete bit.n;
    delete bit.w;
  });

  json.trainings.forEach((trainings: any) => {
    trainings.note = json.notes[trainings.n]?.trim();
    delete trainings.n;
  });
  delete json.notes;

  return json;
}

function outputEncode(json: any) {
  const sortObjectKeysA = <T extends Object>(object: T) => {
    const objectKeys = Object.keys(object) as Array<keyof T>;

    const copy = {...object}

    objectKeys
      .sort()
      .forEach((key: keyof T) => {
        delete object[key];
        object[key] = copy[key];
      });
  };

  const getNotes: (list: any[]) => string[] = list => list.map((bit: any) => bit.note as string)
    .filter(Boolean)
    .map(value => value.trim())
    .filter((value, index, array) => array.indexOf(value) === index);

  const notes = [...getNotes(json.bits), ...getNotes(json.trainings)]
    .filter((value, index, array) => array.indexOf(value) === index)
    .sort();

    json.bits.forEach((bit: any) => {
      bit.s = bit.timestamp;
      bit.t = bit.trainingId;
      bit.v = bit.variationId;
      bit.w = bit.totalWeight;

      const note: string | undefined = bit.note?.trim();
      if (note) {
        bit.n = notes.indexOf(note);
      }

      delete bit.timestamp;
      delete bit.totalWeight;
      delete bit.trainingId;
      delete bit.variationId;
      delete bit.note;
      sortObjectKeysA(bit);
    });

  json.trainings.forEach((trainings: any) => {
    const note: string | undefined = trainings.note?.trim();
    if (note) {
      trainings.n = notes.indexOf(note);
    }
    delete trainings.note;
    sortObjectKeysA(trainings);
  });

  return {
    notes: notes,
    ...json
  };
}