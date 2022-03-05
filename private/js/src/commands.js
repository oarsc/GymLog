export function processCommand(command, data, activeFilters) {
	const keywords = command.toLowerCase().split(' ');
	let instruction = keywords.splice(0,1)[0];

	// remove note [if difícil]
	if (instruction == 'remove') {
		return remove(keywords, data, activeFilters);

	// add[str|int] variation easy [if note easy]
	} else if (instruction == 'addstr') {
		return add(keywords, data, activeFilters, value => value);

	} else if (instruction == 'addint') {
		return add(keywords, data, activeFilters, value => parseInt(value));
	}
}

function remove([fieldName, condIf, value], data, activeFilters) {
	let filter;
	if (condIf == 'if') {
		filter = bit => bit[fieldName] == value;
	} else {
		filter = () => true;
	}

	const filtered = data.bits
		.filter(activeFilters)
		.filter(filter);

	const length = filtered.length;		
	filtered.forEach(bit => delete bit[fieldName]);

	return length;
}

function add([fieldName, value, condIf, condFieldName, condFieldValue], data, activeFilters, processValue) {
	let filter;
	if (condIf == 'if') {
		filter = bit => bit[condFieldName] == condFieldValue;
	} else {
		filter = () => true;
	}

	const filtered = data.bits
		.filter(activeFilters)
		.filter(filter);

	const length = filtered.length;		
	filtered.forEach(bit => bit[fieldName] = processValue(value));

	return length;
}