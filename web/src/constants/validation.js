export const PHONE_REGEX = /^1\d{10}$/;
export const PASSWORD_MIN_LENGTH = 6;
export const PASSWORD_MAX_LENGTH = 20;

export const PHONE_RULE = {
  required: true,
  pattern: PHONE_REGEX,
  message: '手机号格式不正确',
  trigger: ['blur', 'input']
};

export const PASSWORD_RULE = {
  required: true,
  min: PASSWORD_MIN_LENGTH,
  max: PASSWORD_MAX_LENGTH,
  message: '密码长度需在6-20之间',
  trigger: ['blur', 'input']
};

export const isValidPhone = (value = '') => PHONE_REGEX.test(value);

export const isValidPassword = (value = '') =>
  value.length >= PASSWORD_MIN_LENGTH && value.length <= PASSWORD_MAX_LENGTH;

const isPresent = (value) => value !== null && value !== undefined && value !== '';

export const buildRequiredSelectRule = (message) => ({
  validator: (_, value) => isPresent(value),
  message,
  trigger: ['blur', 'change']
});

export const buildRequiredNumberRule = (message, min = 1) => ({
  validator: (_, value) => Number.isFinite(value) && value >= min,
  message,
  trigger: ['blur', 'change']
});
