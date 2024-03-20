/* eslint-env node */
module.exports = {
	root: true,
	'extends': [
		'plugin:vue/vue3-essential',
		'eslint:recommended'
	],
	parserOptions: {
		ecmaVersion: 'latest'
	},
	ignorePatterns: [
		"src/components/ui/**/*.vue",
		"src/components/ui/**/*.js"
	],
	rules: {
		"indent": ["error", "tab"],
		"quotes": ["error", "double"],
		"semi": ["error", "never"],
		"linebreak-style": "off",
		"semi-style": ["error", "last"],
		"brace-style": "error",
		"comma-style": ["error", "last"],
		"operator-linebreak": ["error", "before"],
		"implicit-arrow-linebreak": ["error", "beside"],
		"padded-blocks": ["error", {"classes": "always"}],
		"arrow-spacing": "error",
		"block-spacing": "error",
		"semi-spacing": "error",
		"comma-spacing": ["error", {"before": false, "after": true}],
		"computed-property-spacing": ["error", "never"],
		"array-bracket-spacing": ["error", "never"],
		"func-call-spacing": ["error", "never"],
		"key-spacing": ["error", {"beforeColon": false, "afterColon": true, "mode": "strict"}],
		"object-curly-spacing": ["error", "always"],
		"rest-spread-spacing": ["error", "never"],
		"template-curly-spacing": "error",
		"template-tag-spacing": "error",
		"keyword-spacing": ["error", {"before": true, "after": true}],
		"space-infix-ops": "error",
		"space-unary-ops": "error",
		"space-before-blocks": "error",
		"space-before-function-paren": ["error", "never"],
		"new-parens": "error",
		"space-in-parens": ["error", "never"],
		"function-paren-newline": ["error", "consistent"],
		"function-call-argument-newline": ["error", "consistent"],
		"comma-dangle": ["error", "never"],
		"dot-location": ["error", "property"],
		"eol-last": ["error", "never"],
		"lines-between-class-members": ["error", "always"],
		"max-len": ["error", {"code": 150}],
		"no-multiple-empty-lines": "error",
		"no-tabs": "off",
		"no-trailing-spaces": "error",
		"no-whitespace-before-property": "error",
		"nonblock-statement-body-position": ["error", "beside"],
		"wrap-regex": "error",
		"camelcase": "error"
	}
}
