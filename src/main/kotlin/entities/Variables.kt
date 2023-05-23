package entities

class Variables {
    private val variables: MutableMap<String, Double> = mutableMapOf();

    fun set(name: String, value: Double) {
        this.variables[name] = value;
    }

    fun get(name: String): Double {
        return this.variables[name]?: throw IllegalArgumentException();
    }

    fun getNames(): Set<String> {
        return this.variables.keys;
    }
}
