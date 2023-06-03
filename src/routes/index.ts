import { Router } from "express";
import { writeFile } from "fs";

export const router = Router();

/* GET home page. */
router.get('/', (req, res, next) =>
  res.render('react', { script: './js/main.min.js' })
);

router.get('/calendar', (req, res, next) =>
  res.render('react', { script: './js/calendar.min.js' })
);

router.post('/save', (req, res, next) => {
  writeFile('bak/output.json', JSON.stringify(req.body, null, 1), err => {});
  res.send('Saved')
});