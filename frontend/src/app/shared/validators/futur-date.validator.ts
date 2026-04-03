import { AbstractControl, ValidatorFn, ValidationErrors } from '@angular/forms';

export function futurDateValidator(minDaysAhead: number = 0): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    const inputDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const minDate = new Date(today);
    minDate.setDate(minDate.getDate() + minDaysAhead);

    if (inputDate < minDate) {
      return {
        futureDateInvalid: minDaysAhead > 0
          ? `La date doit etre au moins ${minDaysAhead} jour(s) dans le futur`
          : 'La date doit etre dans le futur'
      };
    }

    return null;
  };
}
