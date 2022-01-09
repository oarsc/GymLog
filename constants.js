module.exports.STATUS = {
	NONE: 0,
	WEIGHT: 1,
	REPS: 2,
	DURATION: 3,
	DONE: 4,
}

module.exports.MAPPING = {
	'Press de Pecho Plano con Barra de Pesas'                                          : 'Barbell Bench Press',
	'Flexiones de Pecho con Mancuernas (inclinado)'                                    : 'Dumbbell Bench Press (incline)',
	'Cruzadas con Cable-Polea (de pie)'                                                : 'Pulley Machine Low Crossover (standing)',
	'Cruzadas con polea baja (de pie)'                                                 : 'Low Pulley Flys (standing)',
	'Flexiones de Pecho con Mancuernas (declinado)'                                    : 'Dumbbell Bench Press (decline)',
	'Abdominales (declinado)'                                                          : 'Crunches (decline)',
	'Abdominales Parciales de Bicicleta'                                               : 'Air Bike Crunches',
	'Dominadas, Agarre Medio por Encima'                                               : 'Pull-ups, Overhand Medium Grip',
	'Espalda con Cable-Polea al Tórax, Agarre Amplio (sentado)'                        : 'Pulley Machine Pulldown To Chest, Wide Grip (seated)',
	'Remo en Banco Plano Alto con Mancuernas (prono)'                                  : 'Dumbbell Flat High Bench Row (prone)',
	'Remo con Cable-Polea (sentado)'                                                   : 'Pulley Machine Row (seated)',
	'Espalda a Tórax con Cable-Polea, Agarre Cerrado de Martillo (sentado)'            : 'Pulley Machine Pulldown To Chest, Hammer Close Grip (seated)',
	'Espalda a Tórax con Cable-Polea, Agarre Cerrado por Debajo (sentado)'             : 'Pulley Machine Pulldown To Chest, Underhand Close Grip (seated)',
	'Levantamiento Frontal con Mancuernas (de pie)'                                    : 'Dumbbell Front Raise (standing)',
	'Levantamiento Lateral con Mancuernas (de pie)'                                    : 'Dumbbell Lateral Raise (standing)',
	'Press de Hombros con Mancuernas (sentado)'                                        : 'Dumbbell Shoulder Press (seated)',
	'Levantamiento Lateral con Cable-Polea en un solo Brazo (inclinado)'               : 'Pulley Machine Lateral Raise, Single Arm (bent over)',
	'Remo Vertical con Barra de Pesas (de pie)'                                        : 'Barbell Upright Row (standing)',
	'Encogimiento de Hombros con Mancuernas (de pie)'                                  : 'Dumbbell Shrugs (standing)',
	'Sentadillas con Barra de Pesas (de pie)'                                          : 'Barbell Squats (standing)',
	'Estocadas con Mancuernas'                                                         : 'Dumbbell Lunges',
	'Máquina de Prensa de Piernas (recto)'                                             : 'Machine Leg Press',
	'Extensión de la Cadera con Máquina'                                               : 'Machine Hip Extension',
	'Flexiones de Pierna con Máquina (de pie)'                                         : 'Machine Leg Curl (standing)',
	'Balanceo de Pantorrillas con Máquina (sentado)'                                   : 'Machine Calf Raise (seated)',
	'Flexiones de Bíceps con Barra EZ (de pie)'                                        : 'EZ-Bar Biceps Curl (standing)',
	'Flexiones de Bíceps Alternando las Mancuernas (sentado)'                          : 'Dumbbell Alternating Biceps Curl (seated)',
	'Flexiones de Bíceps Alternando las Mancuernas en Martillo (sentado)'              : 'Dumbbell Alternating Biceps Hammer Curl (seated)',
	'Flexiones de Bíceps y Antebrazo con una sola Mancuerna'                           : 'Dumbbell Single Arm Biceps Curl',
	'Press de Tríceps Francés con Barra EZ (acostado)'                                 : 'EZ-Bar Triceps French Press (lying)',
	'Arrastre de Tríceps con Cable-Polea, Agarre por Debajo (de pie)'                  : 'Pulley Machine Triceps Press (standing)',
	'Tracción en Barras Paralelas'                                                     : 'Bar Dips',
	'Mariposa con Máquinas (sentado)'                                                  : 'Machine Butterfly (seated)',
	'Press de Hombros con Barra de Pesas (de pie)'                                     : 'Barbell Shoulder Press (standing)',
	'Levantamiento Frontal con Disco (de pie)'                                         : 'Plate Front Raise (standing)',
	'Aperturas en Banco Plano con Mancuernas Inversas (prono)'                         : 'Reverse Dumbbell Flat Bench Flys (prone)',
	'Flexiones de Bíceps con Cable-Polea (de pie)'                                     : 'Pulley Machine Biceps Curl (standing)',
	'Press de Tríceps con Cuerda y Cable-Polea (de pie)'                               : 'Pulley Machine Rope Triceps Press (standing)',
	'Barra de Peso Muerto'                                                             : 'Barbell Deadlift',
	'Patada de Tríceps con Mancuernas (inclinado)'                                     : 'Dumbbell Triceps Kickbacks (bent over)',
	'Levantamiento Lateral con Mancuernas (un brazo)'                                  : 'Dumbbell Lateral Raise, Single Arm (standing)',
	'Press de Pecho Plano con Barra de Pesas, Agarre Amplio (declinado)'               : 'Barbell Bench Press, Wide Grip (decline)',
	'Press de Pecho Frontal con Máquina (sentado)'                                     : 'Machine Front Press (seated)',
	'Máquina de Prensa de Piernas (inclinado)'                                         : 'Machine Leg Press (incline)',
	'Máquina de Aducción (sentado)'                                                    : 'Machine Adduction Press (seated)',
	'Máquina de Abducción (sentado)'                                                   : 'Machine Abduction Press (seated)',
	'Estocadas con Barra de Pesas'                                                     : 'Barbell Lunges',
	'Flexiones de Pierna con Máquina (prono)'                                          : 'Machine Leg Curl (prone)',
	'Flexiones de Bíceps Alternando las Mancuernas (de pie)'                           : 'Dumbbell Alternating Biceps Curl (standing)',
	'Aperturas con Mancuernas en Plano'                                                : 'Dumbbell Bench Flys',
	'Press en Banco con Máquina (inclinado)'                                           : 'Machine Bench Press (incline)',
	'Levantamiento de Piernas, Apoyo en los Brazos'                                    : 'Leg Raise, Rest On Arms',
	'Elevación de la Rodilla, Apoyo en los Brazos'                                     : 'Knee Raise, Rest On Arms',
	'Press de Pecho con Barra de Pesas (declinado)'                                    : 'Barbell Bench Press (decline)',
	'Flexiones de Bíceps con Barra EZ en Máquina Scott (sentado)'                      : 'Scott Machine EZ-Bar Biceps Curl (seated)',
	'Flexiones de Bíceps con Cable-Polea por Arriba (de pie)'                          : 'Overhead Pulley Machine Biceps Curl (standing)',
	'Flexiones de Bíceps con Cuerda en Cable-Polea (de pie)'                           : 'Pulley Machine Rope Biceps Curl (standing)',
	'aaaa cruzado'                                                                     : 'Pulley Machine Crossover (standing)',
	'pecho superior maquina'                                                           : 'Machine Bench Press (incline)',
	'Aperturas con Mancuernas (declinado)'                                             : 'Dumbbell Bench Flys (decline)',
	'Flexiones de Bíceps Alternando las Mancuernas en Martillo (de pie)'               : 'Dumbbell Alternating Biceps Hammer Curl (standing)',
	'Flexiones de Bíceps con Mancuernas (de pie)'                                      : 'Dumbbell Biceps Curl (standing)',
	'Flexiones de Bíceps Concentradas con Mancuernas (sentado)'                        : 'Dumbbell Biceps Concentration Curl (seated)',
	'Press de Tríceps con una Mancuerna con el Brazo Arriba (sentado)'                 : 'Overhead Single Arm Dumbbell Triceps Press (seated)',
	'Levantamiento Frontal con un solo Brazo en Cable-Polea (de pie)'                  : 'Single Arm Pulley Machine Front Raise (standing)',
	'Espalda con Máquina de Palanca (sentado)'                                         : 'Leverage Machine Pulldown (seated)',
	'Aperturas con Mancuernas (inclinado)'                                             : 'Dumbbell Bench Flys (incline)',
	'Remo Vertical con Cable-Polea (de pie)'                                           : 'Pulley Machine Upright Row (standing)',
	'Flexiones de Pecho, Agarre Cerrado'                                               : 'Push-ups, Close Grip',
	'Flexiones de Bíceps en Máquina Scott (sentado)'                                   : 'Scott Machine Biceps Curl (seated)',
	'bi un brazo ladeado'                                                              : 'Dumbbell Biceps Crossbody Curl (standing)',
	'Extensión de Piernas con Máquina (sentado)'                                       : 'Machine Leg Extension (seated)',
	'Balanceo de Pantorrillas con Máquina (de pie)'                                    : 'Machine Calf Raise (standing)',
	'Press de Tríceps con Cuerda y Cable-Polea por Arriba (inclinado)'                 : 'Overhead Pulley Machine Rope Triceps Press (bent over)',
	'Halar con Cable-Polea y Brazo Recto (de pie)'                                     : 'Pulley Machine Lat Pushdown (standing)',
	'Remo con Máquina de Palanca (sentado)'                                            : 'Leverage Machine Row (seated)',
	'Abdominales obliquos con polea (Leñador)'                                         : 'Pulley Oblique Twist (Wood Chopper)',
	'maquina abdos'                                                                    : 'Supported Crunches (lying)',
	'Máquina de Hiperextensiones'                                                      : 'Machine Hyperextensions',
	'Flexiones de Pierna con Máquina (sentado)'                                        : 'Machine Leg Curl (seated)',
	'Press de Pecho con Barra de Pesas (inclinado)'                                    : 'Barbell Bench Press (incline)',
	'Flexiones de Pecho en Plano con Mancuernas'                                       : 'Dumbbell Bench Press',
	'arnold'                                                                           : 'Dumbbell Arnold Press (seated)',
	'Pullover en Banco Plano con Mancuernas'                                           : 'Dumbbell Bench Pullover',
	'Flexiones de Bíceps con Mancuernas en Martillo (de pie)'                          : 'Dumbbell Biceps Hammer Curl (standing)',
	'Abdominales de Giro, Piernas Elevadas'                                            : 'Twisting Sit-ups, Bent Legs',
	'Dominadas en Máquina Sostenida, Agarre Medio por Encima'                          : 'Supported Machine Pull-ups, Overhand Medium Grip',
	'Step up con Barra de Pesas, Inclinado (de pie)'                                   : 'Barbell Tipup, Bent Over (standing)',
	'Abdominales, Piernas Flexionadas'                                                 : 'Crunches, Bent Legs',
	'Abdominales Parciales, Piernas Flexionadas'                                       : 'Crunches, Bent Legs',
	'Apertura con Máquina (sentado)'                                                   : 'Machine Fly (seated)',
	'Levantamiento Lateral con Máquina (sentado)'                                      : 'Machine Lateral Raise (seated)',
	'Aperturas en Banco Plano con Cable-Polea'                                         : 'Pulley Machine Bench Flys',
	'Levantamiento de Piernas (acostado)'                                              : 'Leg Raise (lying)',
	'Aperturas en Banco con Cable-Polea (inclinado)'                                   : 'Pulley Machine Bench Flys (incline)',
	'Flexiones de Antebrazo con Barra de Pesas Inversa(de pie)'                        : 'Reverse Barbell Forearm Curl (standing)',
	'Flexiones de Muñecas y Antebrazo con Barra de Pesas (de pie)'                     : 'Barbell Forearm Wrist Curl (standing)',
	'Press de Tríceps con Mancuernas Ambas con Brazos Arriba (sentado)'                : 'Overhead Both Arm Dumbbell Triceps Press (seated)',
	'Remo con Barra de Pesas, Inclinado (de pie)'                                      : 'Barbell Row, Bent Over (standing)',
	'Sentadillas en Máquina de Palanca (de pie)'                                       : 'Machine Leverage Squats (standing)',
	'Flexiones de Bíceps con Barras (de pie)'                                          : 'Barbell Biceps Curl (standing)',
	'Flexiones de Bíceps con Barra de Pesas, Agarre Cerrado (de pie)'                  : 'Barbell Biceps Curl, Close Grip (standing)',
	'Flexiones de Bíceps con Barra de Pesas, Agarre Amplio (de pie)'                   : 'Barbell Biceps Curl (standing)',
	'Flexiones de Bíceps con Mancuernas (sentado)'                                     : 'Dumbbell Biceps Curl (seated)',
	'Flexiones de Pecho, Agarre Amplio'                                                : 'Push-ups, Wide Grip',
	'Barras Paralelas, Inclinado, Agarre Amplio'                                       : 'Bar Dips, Bent Over, Wide Grip',
	'Rodamiento de Pesas (arrodillado)'                                                : 'Barbell Roller (kneeling)',
	'Press de Tríceps con Cable-Polea por Arriba (arrodillado)'                        : 'Overhead Pulley Machine Triceps Press (kneeling)',
	'Press de Tríceps con Máquina (sentado)'                                           : 'Machine Triceps Press (seated)',
	'Remo abs peso'                                                                    : 'Russian Twists',
	'Press Tate de Tríceps con Mancuernas (acostado)'                                  : 'Dumbbell Triceps Tate Press (lying)',
	'Flexión Lateral del Torso con Mancuernas (de pie)'                                : 'Dumbbell Side Bend (standing)',
	'Barras, Agarre Ancho'                                                             : 'Bar Dips, Wide Grip',
	'dominadas rectas'                                                                 : 'Pull-ups, Vertical Grip',
	'sentadillas soporte'                                                              : 'Ankle Support Free Squats',
	'Abdominales Parciales con Cable-Polea (sentado)'                                  : 'Pulley Machine Crunches (seated)',
	'Patada de Tríceps con Cable-Polea (inclinado)'                                    : 'Pulley Machine Triceps Kickback (bent over)',
	'Levantamiento de Pelvis, Piernas Flexionadas (acostado)'                          : 'Pelvic Lift Lying, Bent Legs (machine)',
	'Face Pull'                                                                        : 'Face Pull',
	'espalda'                                                                          : 'Pulley Machine Lat Pushdown (standing)',
	'bi'                                                                               : 'Pulley Machine Biceps Curl, Single Arm (standing)',
	'tri'                                                                              : 'Pulley Machine Underhand Pushdown, Single Arm (standing)',
	'máquina hombro pajaro'                                                            : 'Shoulder Machine Lateral Raise',
	'Levantamiento Lateral con Mancuernas (máquina)'                                   : 'Shoulder Machine Lateral Raise',
	'Flexiones de Bíceps con Barra de Pesas (inclinado)'                               : 'Barbell Biceps Curl (incline)',
	'Flexiones de Bíceps con Barra de Martillo (inclinado)'                            : 'Scott Machine Hammer Barbell Curl',
	'Flexiones de Bíceps con Barra de Martillo (de pie)'                               : 'Barbell Hammer Biceps Curl (standing)',
	'Levantamiento Lateral con Mancuernas (inclinado)'                                 : 'Dumbbell Lateral Raise (bent over)',
	'Flexión Lateral del Torso con Cable-Polea (de pie)'                               : 'Pulley Machine Side Bend (standing)',
	'Flexiones de Bíceps con Barra EZ, Agarre Amplio (de pie)'                         : 'EZ-Bar Biceps Curl, Wide Grip (standing)',
	'Levantamiento Frontal con Barra de Pesas (de pie)'                                : 'Barbell Front Raise (standing)',
	'Flexiones de Bíceps con Mancuernas en Martillo (sentado)'                         : 'Dumbbell Biceps Hammer Curl (seated)',
	'Press en Banco Plano con Barra de Pesas, Agarre Cerrado'                          : 'Barbell Flat Bench Press, Close Grip',
	'Flexiones de Bíceps con Mancuernas en Martillo en la Máquina de Scott'            : 'Scott Machine Dumbbell Biceps Hammer Curl',
	'Máquina de Hiperextensiones (sentado)'                                            : 'Machine Hyperextensions (seated)',
	'Flexiones de Muñecas y Antebrazo con Mancuernas (sentado)'                        : 'Dumbbell Forearm Wrist Curl (seated)',
	'Flexiones de Muñeca y Antebrazo con Barra de Pesas Detrás de la Espalda (de pie)' : 'Barbell Forearm Wrist Curl Behind Back (standing)',
	'anillas espalda'                                                                  : 'Australian Ring Pull-ups',
	'dominadas anillas'                                                                : 'Ring Pull-ups',
	'Spinning'                                                                         : 'Stationary Cycling',
	'Cinta de correr'                                                                  : 'Treadmill',
	'Elíptica'                                                                         : 'Elliptical (Crosstrainer)',
	'Planks'                                                                           : 'Planks',
//  'BodyPump'                                                                         : 142,
//  'Crossover'                                                                        : 145,
}

module.exports.EXCEPTIONS = {
	'Press de Hombros con Barra de Pesas (de pie)' : {
		'sentado': 'Barbell Shoulder Press (seated)'
	},

	'Elevación de la Rodilla, Apoyo en los Brazos' : {
		'colgado': 'Knee Raise (hanging)'
	},

	'Levantamiento de Piernas, Apoyo en los Brazos' : {
		'colgado': 'Leg Raise (hanging)'
	},


	'Extensión de Piernas con Máquina (sentado)' : {
		'maquina': 'Machine Leg Extension, using plates (seated)',
		'máquina': 'Machine Leg Extension, using plates (seated)',
	},

	'Cruzadas con polea baja (de pie)' : {
		'SPECIAL': 'Low Dumbbell Flys (standing)'
	},

	'Remo con Barra de Pesas, Inclinado (de pie)' : {
		'tumbado': 'TODO'
	}
};

// NOTES:
/*
Cruzadas con polea baja (de pie) -- 
Cruzadas con polea baja (de pie) -- "
Cruzadas con polea baja (de pie) -- +
Cruzadas con polea baja (de pie) -- /\
Cruzadas con polea baja (de pie) -- de pie
Cruzadas con polea baja (de pie) -- mancuernas
Cruzadas con polea baja (de pie) -- mancuernas sentado
Cruzadas con polea baja (de pie) -- polea
Cruzadas con polea baja (de pie) -- poleas
Cruzadas con polea baja (de pie) -- sentado
Cruzadas con polea baja (de pie) -- tumbado
Elevación de la Rodilla, Apoyo en los Brazos -- 
Elevación de la Rodilla, Apoyo en los Brazos -- colgado
Elevación de la Rodilla, Apoyo en los Brazos -- colgado lado
Elevación de la Rodilla, Apoyo en los Brazos -- colgado lateral
Elevación de la Rodilla, Apoyo en los Brazos -- colgado normal
Elevación de la Rodilla, Apoyo en los Brazos -- fallo dedos
Extensión de Piernas con Máquina (sentado) -- 
Extensión de Piernas con Máquina (sentado) -- "
Extensión de Piernas con Máquina (sentado) -- /\
Extensión de Piernas con Máquina (sentado) -- 1 pierna
Extensión de Piernas con Máquina (sentado) -- 1pie
Extensión de Piernas con Máquina (sentado) -- d.corto
Extensión de Piernas con Máquina (sentado) -- d.corto (compartida)
Extensión de Piernas con Máquina (sentado) -- d.corto compartiendo
Extensión de Piernas con Máquina (sentado) -- d.largo juan
Extensión de Piernas con Máquina (sentado) -- dificil
Extensión de Piernas con Máquina (sentado) -- difícil
Extensión de Piernas con Máquina (sentado) -- difícil d.largo dani
Extensión de Piernas con Máquina (sentado) -- difícil➕
Extensión de Piernas con Máquina (sentado) -- facil
Extensión de Piernas con Máquina (sentado) -- fácil
Extensión de Piernas con Máquina (sentado) -- maquina
Extensión de Piernas con Máquina (sentado) -- maquina dura
Extensión de Piernas con Máquina (sentado) -- máquina
Extensión de Piernas con Máquina (sentado) -- máquina "libre"
Extensión de Piernas con Máquina (sentado) -- normal
Levantamiento de Piernas, Apoyo en los Brazos -- 
Levantamiento de Piernas, Apoyo en los Brazos -- colgado
Levantamiento de Piernas, Apoyo en los Brazos -- descanso corto
Levantamiento de Piernas, Apoyo en los Brazos -- fallo dedos
Levantamiento de Piernas, Apoyo en los Brazos -- máquina
Levantamiento de Piernas, Apoyo en los Brazos -- normal
Press de Hombros con Barra de Pesas (de pie) -- 
Press de Hombros con Barra de Pesas (de pie) -- "
Press de Hombros con Barra de Pesas (de pie) -- 4x12,5kg sentado
Press de Hombros con Barra de Pesas (de pie) -- 5x12,5kg sentado
Press de Hombros con Barra de Pesas (de pie) -- barra 20kg de pie
Press de Hombros con Barra de Pesas (de pie) -- sentado
Remo con Barra de Pesas, Inclinado (de pie) -- 
Remo con Barra de Pesas, Inclinado (de pie) -- d.largo juan
Remo con Barra de Pesas, Inclinado (de pie) -- tumbado
/**/


module.exports.BAR = [
	0,
	7.5, // 1
	10,  // 2
	15,  // 3
	20,  // 4
	25,  // 5
]

module.exports.BAR_SETTINGS = {
	'Barra de Peso Muerto'                                                             : 4,
	'Estocadas con Barra de Pesas'                                                     : 4,
	'Flexiones de Antebrazo con Barra de Pesas Inversa(de pie)'                        : 4,
	'Flexiones de Bíceps con Barra de Martillo (de pie)'                               : 2,
	'Flexiones de Bíceps con Barra de Pesas (inclinado)'                               : 1,
	'Flexiones de Bíceps con Barra de Pesas, Agarre Amplio (de pie)'                   : 1,
	'Flexiones de Bíceps con Barra de Pesas, Agarre Cerrado (de pie)'                  : 1,
	'Flexiones de Bíceps con Barra EZ (de pie)'                                        : 1,
	'Flexiones de Bíceps con Barra EZ en Máquina Scott (sentado)'                      : 1,
	'Flexiones de Bíceps con Barra EZ, Agarre Amplio (de pie)'                         : 1,
	'Flexiones de Bíceps con Barras (de pie)'                                          : 1,
	'Flexiones de Muñeca y Antebrazo con Barra de Pesas Detrás de la Espalda (de pie)' : 1,
	'Flexiones de Muñecas y Antebrazo con Barra de Pesas (de pie)'                     : 1,
	'Levantamiento Frontal con Barra de Pesas (de pie)'                                : 1,
	'Press de Hombros con Barra de Pesas (de pie)'                                     : 4,
	'Press de Pecho con Barra de Pesas (declinado)'                                    : 4,
	'Press de Pecho con Barra de Pesas (inclinado)'                                    : 4,
	'Press de Pecho Plano con Barra de Pesas'                                          : 4,
	'Press de Pecho Plano con Barra de Pesas, Agarre Amplio (declinado)'               : 4,
	'Press de Tríceps Francés con Barra EZ (acostado)'                                 : 1,
	'Press en Banco Plano con Barra de Pesas, Agarre Cerrado'                          : 4,
	'Remo con Barra de Pesas, Inclinado (de pie)'                                      : 1,
	'Remo Vertical con Barra de Pesas (de pie)'                                        : 1,
	'Sentadillas con Barra de Pesas (de pie)'                                          : 4,
	'Step up con Barra de Pesas, Inclinado (de pie)'                                   : 4,
};

module.exports.BAR_FORMULA = {
	'Barra de Peso Muerto'                                                             : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Estocadas con Barra de Pesas'                                                     : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Antebrazo con Barra de Pesas Inversa(de pie)'                        : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barra de Martillo (de pie)'                               : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barra de Pesas (inclinado)'                               : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barra de Pesas, Agarre Amplio (de pie)'                   : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barra de Pesas, Agarre Cerrado (de pie)'                  : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barra EZ (de pie)'                                        : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barra EZ en Máquina Scott (sentado)'                      : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barra EZ, Agarre Amplio (de pie)'                         : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Bíceps con Barras (de pie)'                                          : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Muñeca y Antebrazo con Barra de Pesas Detrás de la Espalda (de pie)' : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Flexiones de Muñecas y Antebrazo con Barra de Pesas (de pie)'                     : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Levantamiento Frontal con Barra de Pesas (de pie)'                                : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Press de Hombros con Barra de Pesas (de pie)'                                     : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Press de Pecho con Barra de Pesas (declinado)'                                    : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Press de Pecho con Barra de Pesas (inclinado)'                                    : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Press de Pecho Plano con Barra de Pesas'                                          : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Press de Pecho Plano con Barra de Pesas, Agarre Amplio (declinado)'               : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Press de Tríceps Francés con Barra EZ (acostado)'                                 : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Press en Banco Plano con Barra de Pesas, Agarre Cerrado'                          : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Remo con Barra de Pesas, Inclinado (de pie)'                                      : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Remo Vertical con Barra de Pesas (de pie)'                                        : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Sentadillas con Barra de Pesas (de pie)'                                          : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*2,
	'Step up con Barra de Pesas, Inclinado (de pie)'                                   : (weight, name) => BAR[BAR_SETTINGS[name]] + weight*1,
};