# Design System — FUEL BALANCE

## Fuente visual

Los mockups en `KYVO Pantallas` son la fuente visual principal. Se revisaron muestras de las 11 secciones y se inventariaron los 79 archivos actuales.

## Principios observados

- Fondos blancos o casi blancos con halos púrpura muy suaves.
- Púrpura profundo para superficies de alto énfasis y púrpura brillante para acciones.
- Cards blancas, radios amplios, bordes tenues y elevación contenida.
- Texto principal casi negro; texto secundario gris azulado.
- Acciones primarias grandes y cómodas para uso alrededor del entrenamiento.
- Acciones primarias con gradiente púrpura horizontal y altura mínima de 56 dp.
- Iconos lineales dentro de contenedores circulares o cuadrados suavizados.
- Proteína púrpura, carbohidratos turquesa y grasas ámbar.
- Navegación inferior con Meal Share como acción central de mayor jerarquía.

## Tokens base

Los tokens viven en `core/designsystem`: `Color.kt`, `Typography.kt`, `Shape.kt`, `Spacing.kt` y `Theme.kt`. Los valores se aproximaron visualmente y deberán verificarse contra archivos de diseño editables si estos se entregan.

| Token | Valor inicial | Uso |
|---|---|---|
| PurpleDeep | `#1B0A3D` | Superficies de alto énfasis |
| PurplePrimary | `#6D32F5` | Acciones y selección |
| PurpleAccent | `#8B5CF6` | Gradientes y acento |
| PurpleSoft | `#F1EBFF` | Fondos de iconos y tarjetas |
| Ink | `#101323` | Texto principal |
| Slate | `#667085` | Texto secundario |
| Outline | `#E4E2EA` | Bordes y divisores |
| Canvas | `#FBFAFE` | Fondo claro |
| Protein | `#6D32F5` | Proteína |
| Carbohydrate | `#2CB5A8` | Carbohidratos |
| Fat | `#FFB000` | Grasas |

La escala espacial base es 4, 8, 12, 16, 20, 24, 32 y 40 dp. Los radios base son 8, 12, 16, 20 y 28 dp.

La tipografía actual usa la familia sans-serif del sistema. Los mockups muestran una sans geométrica y, en marketing, una variante condensada; no se añadirá una fuente externa hasta conocer el archivo y su licencia.

## Componentes incorporados

- `KyvoBrandLockup`: imagotipo vectorial oficial suministrado, con variante nocturna.
- `KyvoTextField`: campo con estados de foco, error, teclado y transformación visual.
- `KyvoPrimaryButton`: acción primaria con loading y bloqueo de envíos múltiples.
- `KyvoSecondaryButton`: acción secundaria delineada.

- `KyvoStepProgress`: indicador segmentado de 15 pasos.
- `KyvoOptionCard`: tarjeta seleccionable con borde, check y semántica de radio.
- `KyvoNumericInput`: entrada numérica con teclado, IME y error contextual.
- `WizardActions`: CTA primaria y regreso con targets táctiles mínimos.

El onboarding usa un scaffold común, contenido desplazable y acciones estables. Las pantallas reveal reutilizan superficies y colores semánticos de macronutrientes sin copiar números de los mockups.

Los SVG suministrados para género, edad, altura, peso, fuerza, funcional y cardio se integran como `VectorDrawable` nativos. El asset PNG de hipertrofia se conserva sin redibujarlo; las opciones sin asset entregado usan indicadores tipográficos neutros del sistema.

## Responsive y accesibilidad

- Padding horizontal: 16 dp en teléfonos compactos y 24 dp en anchos estándar.
- Objetivo táctil mínimo: 48 dp.
- Contenido largo: scroll o listas lazy; nunca coordenadas absolutas.
- Insets del sistema: administrados con edge-to-edge y APIs Android.
- Iconos interactivos: `contentDescription`; selección: descripción de estado.
- Texto: sp y tolerancia al escalado del sistema.
