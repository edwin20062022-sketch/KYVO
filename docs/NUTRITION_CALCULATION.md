# Cálculo nutricional de KYVO

El resultado es un punto de partida informativo para personas que entrenan en gimnasio. No sustituye una evaluación médica o nutricional.

## BMR

Se utiliza Mifflin-St Jeor:

```text
BMR = 10 × pesoKg + 6.25 × alturaCm - 5 × edad + constante
```

- Masculino: `+5`
- Femenino: `-161`
- Prefiero no decirlo: `-78`, punto medio explícito de ambas constantes.

La tercera opción existe porque el mockup pregunta género pero la ecuación original requiere otro dato. KYVO no infiere silenciosamente información médica; el reveal informa la estimación neutral.

## Factor de actividad

Se combina actividad laboral con entrenamiento:

| Actividad laboral | Base |
|---|---:|
| Oficina/sedentario | 1.20 |
| Activo | 1.35 |
| Trabajo físico | 1.50 |

Cada sesión semanal suma:

| Entrenamiento | Incremento por día |
|---|---:|
| Fuerza | 0.045 |
| Hipertrofia | 0.050 |
| Funcional | 0.055 |
| Cardio | 0.040 |

El factor final se limita a `1.90`. `TDEE = BMR × factor`.

## Ajuste por objetivo

| Objetivo | Ajuste |
|---|---:|
| Bajar grasa | -15% |
| Ganancia muscular | +10% |
| Recomposición | -5% |
| Mantenimiento | 0% |
| Performance/fuerza | +5% |

Son ajustes moderados. Las calorías objetivo se redondean al múltiplo de 10 más cercano únicamente al final.

## Macronutrientes

- Proteína: `2.0 g/kg` para pérdida de grasa y recomposición; `1.8 g/kg` para los demás objetivos.
- Grasas: `0.9 g/kg` para performance; `0.8 g/kg` para los demás.
- Carbohidratos: calorías restantes después de proteína y grasas.
- Energía: proteína `4 kcal/g`, carbohidratos `4 kcal/g`, grasas `9 kcal/g`.

Los gramos se redondean al entero más cercano. La diferencia energética final queda limitada al pequeño margen propio del redondeo.

## Validación

- Edad: 16–100 años.
- Altura: 120–230 cm.
- Peso: 35–300 kg.
- Entrenamiento: 1–7 días por semana.
- Comidas: 1–8 al día.

Los cálculos internos usan `Double`; no se redondean BMR ni TDEE durante pasos intermedios.
