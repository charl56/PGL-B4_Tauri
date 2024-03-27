export default function getCookie(name: string) {
	const cookies = document.cookie
	const index = cookies.search(name)

	if (index === -1)  return "World !"

	let end = cookies.indexOf(";", index)
	end = end === -1 ? cookies.length : end
	return cookies.slice(index + name.length + 1, end)
}