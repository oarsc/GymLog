import { Router } from "express";

export const router = Router();

/* GET home page. */
router.get('/', (req, res, next) =>
  res.render('react', { script: './js/main.min.js' })
);

router.get('/calendar', (req, res, next) =>
  res.render('react', { script: './js/calendar.min.js' })
);
