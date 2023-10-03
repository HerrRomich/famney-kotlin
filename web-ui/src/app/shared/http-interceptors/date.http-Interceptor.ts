import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { Inject, Injectable, InjectionToken, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

export const DATE_ATTRIBUTE_PATHS = new InjectionToken<string[]>('Injects paths to attributes for parsing into dates.');

@Injectable()
export class DateHttpInterceptor implements HttpInterceptor {
  // Migrated from AngularJS https://raw.githubusercontent.com/Ins87/angular-date-interceptor/master/src/angular-date-interceptor.js
  iso8601 = /^\d{4}-\d\d-\d\d(T\d\d:\d\d:\d\d(\.\d+)?(([+-]\d\d:\d\d)|Z)?)?$/;

  constructor(@Optional() @Inject(DATE_ATTRIBUTE_PATHS) private _paths: string[]) {}

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap({
        next: (event: HttpEvent<any>) => {
          if (event instanceof HttpResponse) {
            const body = event.body;
            this.convertToDate(body);
          }
        },
        error: (err: any) => {
          if (err instanceof HttpErrorResponse) {
            if (err.status === 401) {
            }
          }
        },
      }),
    );
  }

  convertToDate(body: any, parentPath?: string) {
    if (body === null || body === undefined) {
      return body;
    }

    if (typeof body !== 'object') {
      return body;
    }

    for (const key of Object.keys(body)) {
      const path = parentPath === undefined ? key : `${parentPath}->${key}`;
      const value = body[key];
      if (this.isIso8601(value, path)) {
        body[key] = new Date(value);
      } else if (typeof value === 'object') {
        this.convertToDate(value, path);
      }
    }
  }

  isIso8601(value: any, path: string) {
    return typeof value === 'string' && this.iso8601.test(value) && this._paths.some((ending) => path.endsWith(ending));
  }
}
