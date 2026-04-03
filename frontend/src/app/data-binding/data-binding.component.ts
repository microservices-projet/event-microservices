import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-data-binding',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './data-binding.component.html',
  styleUrl: './data-binding.component.css'
})
export class DataBindingComponent {
  a:string="bonjour";
  color:string="red";
  plc="name";
  nom=""

  clickMe(){
    alert("vous avez cliquer")
  }
}
