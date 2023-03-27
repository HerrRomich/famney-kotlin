
export type DateFormatName = 'MMM YYYY' | 'L';

const dateFormatters: Record<DateFormatName, Intl.DateTimeFormatOptions> = {
  'MMM YYYY': {
    month: "short",
    year: "numeric",
  },
  'L': {
    dateStyle: 'short',
  }
};

export class LocaleService {
  private _dateFormatters: Partial<Record<keyof (typeof dateFormatters), Intl.DateTimeFormat>> = {};

  constructor(readonly locale: string) {
  }

  formatDate(date: Date, dateFormatName: DateFormatName){
    const format = this._dateFormatters[dateFormatName] ?? this.initFormat(dateFormatName);
    return format.format(date);
  }

  initFormat(dateFormatName: DateFormatName) {
    const format = Intl.DateTimeFormat(this.locale, dateFormatters[dateFormatName]);
    this._dateFormatters[dateFormatName] = format;
    return format;
  }
}
