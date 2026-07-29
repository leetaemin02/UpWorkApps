module.exports = {
  env: {
    es6: true,
    node: true,
  },
  parserOptions: {
    ecmaVersion: 2022,
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  rules: {
    "no-restricted-globals": ["error", "name", "length"],
    "prefer-arrow-callback": "error",
    "quotes": ["error", "double", {"allowTemplateLiterals": true}],
    "max-len": ["off"],
    "valid-jsdoc": "off",
    "require-jsdoc": "off",
    "object-curly-spacing": "off",
  },
  overrides: [
    {
      files: ["**/*.spec.js"],
      env: {
        mocha: true,
      },
      rules: {},
    },
  ],
};
