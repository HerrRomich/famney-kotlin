/**
 * Master data
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { IncomeCategoryDto } from './income-category.dto';
import { ExpenseCategoryDto } from './expense-category.dto';


export interface EntryCategoriesDto { 
    incomes: Array<IncomeCategoryDto>;
    expenses: Array<ExpenseCategoryDto>;
}


