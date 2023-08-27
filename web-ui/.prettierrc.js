module.exports =  {
  "singleQuote": true,
  "printWidth": 120,
  "tabWidth": 2,
  "useTabs": false,
  "arrowParens": "always",
  "trailingComma": "all",
  "quoteProps": "preserve",
  "bracketSameLine": false,
  "overrides": [
    {
      "files": "*.html",
      "options": {
        "singleQuote": false,
        "parser": "html"
      },
    },
    {
      "files": "*.component.html",
      "options": {
        "singleQuote": false,
        "parser": "angular"
      }
    }
  ],
  "plugins": [
    "prettier-plugin-organize-imports"
  ]
};
