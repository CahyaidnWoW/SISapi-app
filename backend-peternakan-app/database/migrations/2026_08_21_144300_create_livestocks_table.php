<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
{
    Schema::create('livestocks', function (Blueprint $table) {
        $table->id();
        $table->string('tag_number')->unique();
        $table->foreignId('cage_id')->constrained('cages')->cascadeOnDelete();
        $table->foreignId('breed_id')->constrained('livestock_breeds')->cascadeOnDelete();
        $table->enum('gender', ['male', 'female']);
        $table->date('birth_date')->nullable();
        $table->date('entry_date');
        $table->decimal('initial_weight', 8, 2);
        $table->enum('status', ['healthy', 'sick', 'sold', 'dead'])->default('healthy');
        $table->timestamps();
    });
}

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('livestocks');
    }
};
