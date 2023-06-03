import { Router } from 'express';
import * as fs from 'fs';
import path from 'path';
import { handleError } from '../error';

export const router = Router();

/* GET users listing. */
router.get('/', (req, res, next) => {
  fs.readFile(path.join(__dirname, '../../bak/output.json'), 'utf8', (err, content) => {
    handleError(err, 404, next, () => {
      res.json(JSON.parse(content));
    });
  });
});